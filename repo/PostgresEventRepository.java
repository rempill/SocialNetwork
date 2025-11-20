package repo;

import domain.Duck;
import domain.Event;
import domain.RaceEvent;
import domain.User;
import errors.RepoError;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * JDBC-based repository for persisting Event and RaceEvent entities
 * in a PostgreSQL database.
 *
 * Tables used:
 *   events(id, name, type)
 *   race_events(event_id FK -> events.id, lanes, distances)
 *   race_participants(race_event_id FK -> race_events.event_id, duck_id FK -> duck.id, lane)
 *   event_subscribers(event_id FK -> events.id, user_id FK -> user_base.id)
 *   event_notifications(id, event_id FK -> events.id, message, created_at)
 */
public class PostgresEventRepository implements EventRepository {
    private final String url;
    private final String user;
    private final String password;
    private final UserRepository userRepository;

    public PostgresEventRepository(String url, String user, String password, UserRepository userRepository) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public Event findOne(Integer id) {
        if (id == null) throw new IllegalArgumentException("id is null");

        try (Connection c = getConnection()) {
            // Load base event
            String sqlEvent = "SELECT id, name, type FROM events WHERE id = ?";
            Event event;
            try (PreparedStatement ps = c.prepareStatement(sqlEvent)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    String name = rs.getString("name");
                    String type = rs.getString("type");
                    if ("RACE".equals(type)) {
                        event = loadRaceEvent(c, id, name);
                    } else {
                        event = new Event(id, name);
                    }
                }
            }

            // Load subscribers
            String sqlSubs = "SELECT user_id FROM event_subscribers WHERE event_id = ?";
            try (PreparedStatement ps = c.prepareStatement(sqlSubs)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int userId = rs.getInt("user_id");
                        User subscriber = userRepository.findOne(userId);
                        if (subscriber != null) {
                            event.attachSubscriber(subscriber);
                        }
                    }
                }
            }

            // Load notifications
            String sqlNotifs = "SELECT message FROM event_notifications WHERE event_id = ? ORDER BY created_at";
            try (PreparedStatement ps = c.prepareStatement(sqlNotifs)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        event.appendNotification(rs.getString("message"));
                    }
                }
            }

            return event;

        } catch (SQLException e) {
            throw new RepoError("DB findOne error: " + e.getMessage());
        }
    }

    private RaceEvent loadRaceEvent(Connection c, int id, String name) throws SQLException {
        String sql = "SELECT lanes, distances FROM race_events WHERE event_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new RepoError("RaceEvent data missing for id " + id);
                int lanes = rs.getInt("lanes");
                Array distancesArray = rs.getArray("distances");
                Double[] distances = (Double[]) distancesArray.getArray();
                RaceEvent race = new RaceEvent(id, name, lanes);
                race.setDistances(Arrays.stream(distances).mapToDouble(Double::doubleValue).toArray());

                // load participants
                String sqlPart = "SELECT duck_id, lane FROM race_participants WHERE race_event_id = ? ORDER BY lane";
                try (PreparedStatement psPart = c.prepareStatement(sqlPart)) {
                    psPart.setInt(1, id);
                    try (ResultSet rsPart = psPart.executeQuery()) {
                        while (rsPart.next()) {
                            int duckId = rsPart.getInt("duck_id");
                            User participant = userRepository.findOne(duckId);
                            if (participant instanceof Duck duck) {
                                race.getParticipants().add(duck);
                            }
                        }
                    }
                }
                return race;
            }
        }
    }

    @Override
    public Iterable<Event> findAll() {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT id FROM events ORDER BY id";
        try (Connection c = getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                events.add(findOne(id));
            }
        } catch (SQLException e) {
            throw new RepoError("DB findAll error: " + e.getMessage());
        }
        return events;
    }

    @Override
    public Event save(Event entity) throws RepoError {
        if (entity == null) throw new IllegalArgumentException("entity is null");
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);

            // Insert into events
            String sqlEvent = "INSERT INTO events(name, type) VALUES (?, ?) RETURNING id";
            int generatedId;
            try (PreparedStatement ps = c.prepareStatement(sqlEvent)) {
                ps.setString(1, entity.getName());
                ps.setString(2, entity instanceof RaceEvent ? "RACE" : "EVENT");
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new RepoError("Failed to generate event id");
                    generatedId = rs.getInt(1);
                }
            }

            // Insert race-specific data
            if (entity instanceof RaceEvent race) {
                String sqlRace = "INSERT INTO race_events(event_id, lanes, distances) VALUES (?, ?, ?)";
                try (PreparedStatement ps = c.prepareStatement(sqlRace)) {
                    ps.setInt(1, generatedId);
                    ps.setInt(2, race.getLanes());
                    ps.setArray(3, c.createArrayOf("double precision", Arrays.stream(race.getDistances()).boxed().toArray(Double[]::new)));
                    ps.executeUpdate();
                }

                // Insert participants
                String sqlPart = "INSERT INTO race_participants(race_event_id, duck_id, lane) VALUES (?, ?, ?)";
                try (PreparedStatement ps = c.prepareStatement(sqlPart)) {
                    int lane = 1;
                    for (Duck d : race.getParticipants()) {
                        ps.setInt(1, generatedId);
                        ps.setInt(2, d.getId());
                        ps.setInt(3, lane++);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
            entity.setId(generatedId);
            c.commit();
            return entity;
        } catch (SQLException e) {
            throw new RepoError("DB save error: " + e.getMessage());
        }
    }

    @Override
    public Event delete(Integer id) {
        if (id == null) throw new IllegalArgumentException("id is null");
        Event existing = findOne(id);
        if (existing == null) return null;
        String sql = "DELETE FROM events WHERE id = ?"; // cascades to race_events, participants, subscribers, notifications
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return existing;
        } catch (SQLException e) {
            throw new RepoError("DB delete error: " + e.getMessage());
        }
    }

    public void addSubscriber(int eventId, int userId) {
        String sql = "INSERT INTO event_subscribers(event_id, user_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepoError("DB addSubscriber error: " + e.getMessage());
        }
    }

    public void removeSubscriber(int eventId, int userId) {
        String sql = "DELETE FROM event_subscribers WHERE event_id = ? AND user_id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepoError("DB removeSubscriber error: " + e.getMessage());
        }
    }

    public void addNotification(int eventId, String message) {
        String sql = "INSERT INTO event_notifications(event_id, message) VALUES (?, ?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setString(2, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepoError("DB addNotification error: " + e.getMessage());
        }
    }
}

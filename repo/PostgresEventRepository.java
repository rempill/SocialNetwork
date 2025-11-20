package repo;

import domain.*;
import errors.RepoError;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JDBC-based repository for persisting {@link Event} entities in a PostgreSQL database.
 * This implementation uses two tables:
 *   event(id, name)
 *   event_subscription(event_id FK -> event.id, user_id FK -> user_base.id)
 *
 * Subscriber relationships are persisted in event_subscription and loaded by findAll.
 */
public class PostgresEventRepository implements EventRepository {
    private final String url;
    private final String user;
    private final String password;

    public PostgresEventRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public Event findOne(Integer id) {
        if (id == null) throw new IllegalArgumentException("id is null");
        String sql = "SELECT id, name FROM event WHERE id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                int eventId = rs.getInt("id");
                String name = rs.getString("name");
                return new Event(eventId, name);
            }
        } catch (SQLException e) {
            throw new RepoError("DB findOne error: " + e.getMessage());
        }
    }

    @Override
    public Iterable<Event> findAll() {
        Map<Integer, Event> map = new LinkedHashMap<>();
        String sqlEvents = "SELECT id, name FROM event";
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            try (ResultSet rs = st.executeQuery(sqlEvents)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    map.put(id, new Event(id, name));
                }
            }
            loadSubscriptions(c, map);
        } catch (SQLException e) {
            throw new RepoError("DB findAll error: " + e.getMessage());
        }
        return map.values();
    }

    private void loadSubscriptions(Connection c, Map<Integer, Event> events) throws SQLException {
        if (events.isEmpty()) return;
        String sql = "SELECT es.event_id, es.user_id, ub.username, ub.email, ub.password " +
                     "FROM event_subscription es " +
                     "JOIN user_base ub ON es.user_id = ub.id";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int eventId = rs.getInt("event_id");
                int userId = rs.getInt("user_id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String pass = rs.getString("password");
                
                Event event = events.get(eventId);
                if (event != null) {
                    // Load the full user entity (Persoana or Duck)
                    User user = loadUser(c, userId, username, email, pass);
                    if (user != null) {
                        event.subscribe(user);
                    }
                }
            }
        }
    }

    private User loadUser(Connection c, int id, String username, String email, String pass) throws SQLException {
        // Try loading as Persoana first
        User user = loadPersoana(c, id, username, email, pass);
        if (user != null) return user;
        // Otherwise try loading as Duck
        return loadDuck(c, id, username, email, pass);
    }

    private Persoana loadPersoana(Connection c, int id, String username, String email, String pass) throws SQLException {
        String sql = "SELECT nume, prenume, ocupatie, data_nasterii, nivel_empatie FROM persoana WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String nume = rs.getString("nume");
                String prenume = rs.getString("prenume");
                String ocupatie = rs.getString("ocupatie");
                java.sql.Date dn = rs.getDate("data_nasterii");
                java.time.LocalDate dataNasterii = dn != null ? dn.toLocalDate() : java.time.LocalDate.now();
                int nivelEmpatie = rs.getInt("nivel_empatie");
                return new Persoana(id, username, email, pass, nume, prenume, ocupatie, dataNasterii, nivelEmpatie);
            }
        }
    }

    private Duck loadDuck(Connection c, int id, String username, String email, String pass) throws SQLException {
        String sql = "SELECT tip_rata, viteza, rezistenta FROM duck WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String tipRataStr = rs.getString("tip_rata");
                double viteza = rs.getDouble("viteza");
                double rezistenta = rs.getDouble("rezistenta");
                TipRata tip = TipRata.valueOf(tipRataStr);
                return switch (tip) {
                    case FLYING -> new FlyingDuck(id, username, email, pass, viteza, rezistenta);
                    case SWIMMING -> new SwimmingDuck(id, username, email, pass, viteza, rezistenta);
                    case FLYING_AND_SWIMMING -> new AmphibiousDuck(id, username, email, pass, viteza, rezistenta);
                };
            }
        }
    }

    @Override
    public Event save(Event entity) throws RepoError {
        if (entity == null) throw new IllegalArgumentException("event is null");
        if (findOne(entity.getId()) != null) {
            throw new RepoError("Event with id " + entity.getId() + " already exists.");
        }
        String sql = "INSERT INTO event(id, name) VALUES(?,?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, entity.getId());
            ps.setString(2, entity.getName());
            ps.executeUpdate();
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
        String sql = "DELETE FROM event WHERE id = ?"; // cascades to event_subscription
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return existing;
        } catch (SQLException e) {
            throw new RepoError("DB delete error: " + e.getMessage());
        }
    }

    /**
     * Save a subscription relationship between an event and a user.
     *
     * @param eventId the event id
     * @param userId the user id
     */
    public void saveSubscription(int eventId, int userId) {
        String sql = "INSERT INTO event_subscription(event_id, user_id) VALUES(?,?) ON CONFLICT DO NOTHING";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepoError("DB saveSubscription error: " + e.getMessage());
        }
    }

    /**
     * Delete a subscription relationship between an event and a user.
     *
     * @param eventId the event id
     * @param userId the user id
     */
    public void deleteSubscription(int eventId, int userId) {
        String sql = "DELETE FROM event_subscription WHERE event_id = ? AND user_id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepoError("DB deleteSubscription error: " + e.getMessage());
        }
    }
}

package repo;

import domain.Card;
import domain.Duck;
import domain.User;
import errors.RepoError;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JDBC-backed repository for {@link Card} aggregates.
 */
public class PostgresCardRepository implements CardRepository {
    private final String url;
    private final String user;
    private final String password;
    private final UserRepository userRepository;

    public PostgresCardRepository(String url, String user, String password, UserRepository userRepository) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public Card findOne(Integer id) {
        if (id == null) throw new IllegalArgumentException("id is null");
        String sql = "SELECT id, nume_card FROM card WHERE id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Card card = new Card(rs.getInt("id"), rs.getString("nume_card"));
                loadMembers(c, card);
                return card;
            }
        } catch (SQLException e) {
            throw new RepoError("DB findOne error: " + e.getMessage());
        }
    }

    private void loadMembers(Connection c, Card card) throws SQLException {
        String sql = "SELECT duck_id FROM card_duck WHERE card_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, card.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int duckId = rs.getInt("duck_id");
                    User member = userRepository.findOne(duckId);
                    if (member instanceof Duck duck) {
                        card.addDuck(duck);
                    }
                }
            }
        }
    }

    @Override
    public Iterable<Card> findAll() {
        List<Card> cards = new ArrayList<>();
        String sql = "SELECT id FROM card ORDER BY id";
        try (Connection c = getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                cards.add(findOne(rs.getInt("id")));
            }
        } catch (SQLException e) {
            throw new RepoError("DB findAll error: " + e.getMessage());
        }
        return cards;
    }

    @Override
    public Card save(Card entity) throws RepoError {
        if (entity == null) throw new IllegalArgumentException("entity is null");
        String sql = "INSERT INTO card(nume_card) VALUES (?) RETURNING id";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, entity.getNumeCard());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    // create a new Card instance with the generated id
                    return new Card(generatedId, entity.getNumeCard());
                } else {
                    throw new RepoError("Failed to generate card id");
                }
            }
        } catch (SQLException e) {
            throw new RepoError("DB save error: " + e.getMessage());
        }
    }

    @Override
    public Card delete(Integer id) {
        if (id == null) throw new IllegalArgumentException("id is null");
        Card existing = findOne(id);
        if (existing == null) return null;
        String sql = "DELETE FROM card WHERE id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return existing;
        } catch (SQLException e) {
            throw new RepoError("DB delete error: " + e.getMessage());
        }
    }

    @Override
    public void addDuck(int cardId, int duckId) {
        String sql = "INSERT INTO card_duck(card_id, duck_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cardId);
            ps.setInt(2, duckId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepoError("DB addDuck error: " + e.getMessage());
        }
    }

    @Override
    public void removeDuck(int cardId, int duckId) {
        String sql = "DELETE FROM card_duck WHERE card_id = ? AND duck_id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, cardId);
            ps.setInt(2, duckId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepoError("DB removeDuck error: " + e.getMessage());
        }
    }

    @Override
    public void removeDuckFromAll(int duckId) {
        String sql = "DELETE FROM card_duck WHERE duck_id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, duckId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepoError("DB removeDuckFromAll error: " + e.getMessage());
        }
    }
}

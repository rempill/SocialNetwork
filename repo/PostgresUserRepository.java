package repo;

import domain.*;
import errors.RepoError;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JDBC-based repository for persisting {@link User} entities (Persoana and Duck subclasses)
 * in a PostgreSQL database. This implementation uses three tables:
 *   user_base(id, username, email, password)
 *   persoana(id FK -> user_base.id, nume, prenume, ocupatie, data_nasterii, nivel_empatie)
 *   duck(id FK -> user_base.id, tip_rata, viteza, rezistenta)
 *   user_friend(user_id, friend_id) symmetrical undirected friendship (stored with user_id < friend_id)
 *
 * Friend relationships now persisted in user_friend; loaded by findAll. findOne currently loads only the base entity.
 */
public class PostgresUserRepository implements UserRepository {
    private final String url;
    private final String user;
    private final String password;

    public PostgresUserRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public User findOne(Integer id) {
        if (id == null) throw new IllegalArgumentException("id is null");
        String sqlBase = "SELECT id, username, email, password FROM user_base WHERE id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sqlBase)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                int uid = rs.getInt("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String pass = rs.getString("password");
                User result = loadPersoana(c, uid, username, email, pass);
                if (result != null) return result;
                result = loadDuck(c, uid, username, email, pass);
                return result != null ? result : null;
            }
        } catch (SQLException e) {
            throw new RepoError("DB findOne error: " + e.getMessage());
        }
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
                LocalDate dataNasterii = dn != null ? dn.toLocalDate() : LocalDate.now();
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
    public Iterable<User> findAll() {
        Map<Integer, User> map = new LinkedHashMap<>();
        String sqlPers = "SELECT ub.id, ub.username, ub.email, ub.password, p.nume, p.prenume, p.ocupatie, p.data_nasterii, p.nivel_empatie " +
                "FROM user_base ub JOIN persoana p ON ub.id = p.id";
        String sqlDuck = "SELECT ub.id, ub.username, ub.email, ub.password, d.tip_rata, d.viteza, d.rezistenta " +
                "FROM user_base ub JOIN duck d ON ub.id = d.id";
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            try (ResultSet rs = st.executeQuery(sqlPers)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    String pass = rs.getString("password");
                    String nume = rs.getString("nume");
                    String prenume = rs.getString("prenume");
                    String ocupatie = rs.getString("ocupatie");
                    java.sql.Date dn = rs.getDate("data_nasterii");
                    LocalDate dataNasterii = dn != null ? dn.toLocalDate() : LocalDate.now();
                    int nivelEmpatie = rs.getInt("nivel_empatie");
                    map.put(id, new Persoana(id, username, email, pass, nume, prenume, ocupatie, dataNasterii, nivelEmpatie));
                }
            }
            try (ResultSet rs = st.executeQuery(sqlDuck)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    String pass = rs.getString("password");
                    String tipRataStr = rs.getString("tip_rata");
                    double viteza = rs.getDouble("viteza");
                    double rezistenta = rs.getDouble("rezistenta");
                    TipRata tip = TipRata.valueOf(tipRataStr);
                    User duck = switch (tip) {
                        case FLYING -> new FlyingDuck(id, username, email, pass, viteza, rezistenta);
                        case SWIMMING -> new SwimmingDuck(id, username, email, pass, viteza, rezistenta);
                        case FLYING_AND_SWIMMING -> new AmphibiousDuck(id, username, email, pass, viteza, rezistenta);
                    };
                    map.put(id, duck);
                }
            }
            loadFriendships(c, map);
        } catch (SQLException e) {
            throw new RepoError("DB findAll error: " + e.getMessage());
        }
        return map.values();
    }

    private void loadFriendships(Connection c, Map<Integer, User> map) throws SQLException {
        if (map.isEmpty()) return;
        String sql = "SELECT user_id, friend_id FROM user_friend";
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int u1 = rs.getInt("user_id");
                int u2 = rs.getInt("friend_id");
                User user1 = map.get(u1);
                User user2 = map.get(u2);
                if (user1 != null && user2 != null) {
                    user1.addFriend(user2);
                    user2.addFriend(user1);
                }
            }
        }
    }

    public void saveFriendship(int id1, int id2) {
        if (id1 == id2) throw new RepoError("Cannot friend self");
        int a = Math.min(id1, id2);
        int b = Math.max(id1, id2);
        String sql = "INSERT INTO user_friend(user_id, friend_id) VALUES(?,?) ON CONFLICT DO NOTHING";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, a);
            ps.setInt(2, b);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepoError("DB saveFriendship error: " + e.getMessage());
        }
    }

    public void deleteFriendship(int id1, int id2) {
        int a = Math.min(id1, id2);
        int b = Math.max(id1, id2);
        String sql = "DELETE FROM user_friend WHERE user_id = ? AND friend_id = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, a);
            ps.setInt(2, b);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepoError("DB deleteFriendship error: " + e.getMessage());
        }
    }

    @Override
    public User save(User entity) throws RepoError {
        if (entity == null) throw new IllegalArgumentException("user is null");
        String sqlBase = "INSERT INTO user_base(username, email, password) VALUES(?,?,?)";
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            int generatedId;
            try (PreparedStatement ps = c.prepareStatement(sqlBase,Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, entity.getUsername());
                ps.setString(2, entity.getEmail());
                ps.setString(3, entity.getPassword());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        entity.setId(generatedId);
                    } else {
                        c.rollback();
                        throw new RepoError("DB save error: failed to retrieve generated id");
                    }
                }
            }
            if (entity instanceof Persoana p) {
                String sqlP = "INSERT INTO persoana(id,nume, prenume, ocupatie, data_nasterii, nivel_empatie) VALUES(?,?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sqlP)) {
                    ps.setInt(1, generatedId);
                    ps.setString(2, p.getNume());
                    ps.setString(3, p.getPrenume());
                    ps.setString(4, p.getOcupatie());
                    ps.setDate(5, java.sql.Date.valueOf(p.getDataNasterii()));
                    ps.setInt(6, p.getNivelEmpatie());
                    ps.executeUpdate();
                }
            } else if (entity instanceof Duck d) {
                String sqlD = "INSERT INTO duck(id,tip_rata, viteza, rezistenta) VALUES(?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sqlD)) {
                    ps.setInt(1, generatedId);
                    ps.setString(2, d.getTipRata().name());
                    ps.setDouble(3, d.getViteza());
                    ps.setDouble(4, d.getRezistenta());
                    ps.executeUpdate();
                }
            } else {
                throw new RepoError("Unknown user subtype");
            }
            c.commit();
            return entity;
        } catch (SQLException e) {
            throw new RepoError("DB save error: " + e.getMessage());
        }
    }

    @Override
    public User delete(Integer id) {
        if (id == null) throw new IllegalArgumentException("id is null");
        User existing = findOne(id);
        if (existing == null) return null;
        String sqlBase = "DELETE FROM user_base WHERE id = ?"; // cascades to subtype tables & friendships
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sqlBase)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return existing;
        } catch (SQLException e) {
            throw new RepoError("DB delete error: " + e.getMessage());
        }
    }
}

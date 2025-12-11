package repo;

import domain.*;
import errors.RepoError;
import util.PageResult;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    Connection c;

    public PostgresUserRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        try{
            c = getConnection();
        }
        catch(Exception e){
            System.out.println("Connection failed: " + e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public User findOne(Integer id) {
        if (id == null) throw new IllegalArgumentException("id is null");
        String sqlBase = "SELECT id, username, email, password FROM user_base WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sqlBase)) {
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
                return buildDuckEntity(id, username, email, pass,
                        rs.getString("tip_rata"), rs.getDouble("viteza"), rs.getDouble("rezistenta"));
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
        try (Statement st = c.createStatement()) {
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
                    map.put(id, buildDuckEntity(id, username, email, pass,
                            rs.getString("tip_rata"), rs.getDouble("viteza"), rs.getDouble("rezistenta")));
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
        try (PreparedStatement ps = c.prepareStatement(sql)) {
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
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, a);
            ps.setInt(2, b);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepoError("DB deleteFriendship error: " + e.getMessage());
        }
    }

    @Override
    public User findByEmail(String email) {
        if (email == null) {
            return null;
        }
        String sql = "SELECT id FROM user_base WHERE email = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return findOne(rs.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RepoError("DB findByEmail error: " + e.getMessage());
        }
    }

    @Override
    public boolean emailExists(String email) {
        if (email == null) {
            return false;
        }
        String sql = "SELECT 1 FROM user_base WHERE email = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RepoError("DB emailExists error: " + e.getMessage());
        }
    }

    @Override
    public User save(User entity) throws RepoError {
        if (entity == null) throw new IllegalArgumentException("user is null");
        if (emailExists(entity.getEmail())) {
            throw new RepoError("Email already exists");
        }
        String sqlBase = "INSERT INTO user_base(username, email, password) VALUES(?,?,?)";
        try{
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
        try (PreparedStatement ps = c.prepareStatement(sqlBase)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return existing;
        } catch (SQLException e) {
            throw new RepoError("DB delete error: " + e.getMessage());
        }
    }

    @Override
    public Iterable<Duck> findAllDucks() {
        return findDuckPage(0, Integer.MAX_VALUE, null).getItems();
    }

    @Override
    public PageResult<User> findPage(int pageIndex, int pageSize) {
        return fetchPage(pageIndex, pageSize, null);
    }

    @Override
    public PageResult<Duck> findDuckPage(int pageIndex, int pageSize, TipRata filter) {
        PageSpec spec = (filter == null)
                ? PageSpec.duck()
                : switch (filter) {
                    case FLYING -> PageSpec.flying();
                    case SWIMMING -> PageSpec.swimming();
                    case FLYING_AND_SWIMMING -> PageSpec.amphibious();
                };
        PageResult<User> page = fetchPage(pageIndex, pageSize, spec);
        return new PageResult<>(page.getItems().stream().map(Duck.class::cast).toList(), pageIndex, pageSize, page.getTotalItems());
    }

    private PageResult<User> fetchPage(int pageIndex, int pageSize, PageSpec spec) {
        // fetches a page of users according to the given PageSpec (or all if null)
        if (pageIndex < 0 || pageSize <= 0) {
            throw new RepoError("Invalid pagination arguments");
        }
        PageSpec effective = spec != null ? spec : PageSpec.all();
        long total = countEntities(effective);
        Map<Integer, User> items = new LinkedHashMap<>();
        try (PreparedStatement ps = c.prepareStatement(effective.sql)) {
            ps.setInt(1, pageIndex * pageSize);
            ps.setInt(2, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.putAll(createUserFromRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RepoError("DB pagination error: " + e.getMessage());
        }
        try {
            loadFriendships(c, items);
        } catch (SQLException e) {
            throw new RepoError("DB load friendships error: " + e.getMessage());
        }
        return new PageResult<>(items.values().stream().toList(), pageIndex, pageSize, total);
    }

    private long countEntities(PageSpec spec) {
        try (PreparedStatement ps = c.prepareStatement(spec.countSql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new RepoError("DB count error: " + e.getMessage());
        }
    }

    private Map<Integer, User> createUserFromRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String email = rs.getString("email");
        String pass = rs.getString("password");
        Map<Integer, User> result = new LinkedHashMap<>();
        if (rs.getString("nume") != null) {
            String nume = rs.getString("nume");
            String prenume = rs.getString("prenume");
            String ocupatie = rs.getString("ocupatie");
            java.sql.Date dn = rs.getDate("data_nasterii");
            LocalDate dataNasterii = dn != null ? dn.toLocalDate() : LocalDate.now();
            int nivelEmpatie = rs.getInt("nivel_empatie");
            result.put(id, new Persoana(id, username, email, pass, nume, prenume, ocupatie, dataNasterii, nivelEmpatie));
        } else {
            result.put(id, buildDuckEntity(id, username, email, pass,
                    rs.getString("tip_rata"), rs.getDouble("viteza"), rs.getDouble("rezistenta")));
        }
        return result;
    }

    private Duck buildDuckEntity(int id, String username, String email, String pass, String tipRataStr, double viteza, double rezistenta) {
        TipRata tip = TipRata.valueOf(tipRataStr);
        return switch (tip) {
            case FLYING -> new FlyingDuck(id, username, email, pass, viteza, rezistenta);
            case SWIMMING -> new SwimmingDuck(id, username, email, pass, viteza, rezistenta);
            case FLYING_AND_SWIMMING -> new AmphibiousDuck(id, username, email, pass, viteza, rezistenta);
        };
    }

    @Override
    public void updatePassword(int userId, String hashedPassword) {
        String sql = "UPDATE user_base SET password = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepoError("DB updatePassword error: " + e.getMessage());
        }
    }

    private record PageSpec(String sql, String countSql) {
        static PageSpec all() {
            // prepares query for all users (Persoana and Duck)
            String base = "WITH ordered AS (" +
                    "SELECT ub.id, ub.username, ub.email, ub.password, p.nume, p.prenume, p.ocupatie, p.data_nasterii, p.nivel_empatie, " +
                    "NULL AS tip_rata, NULL AS viteza, NULL AS rezistenta FROM user_base ub JOIN persoana p ON ub.id = p.id " +
                    "UNION ALL " +
                    "SELECT ub.id, ub.username, ub.email, ub.password, NULL, NULL, NULL, NULL, NULL, d.tip_rata, d.viteza, d.rezistenta FROM user_base ub JOIN duck d ON ub.id = d.id) " +
                    "SELECT * FROM ordered ORDER BY id OFFSET ? LIMIT ?";
            String count = "SELECT (SELECT COUNT(*) FROM persoana) + (SELECT COUNT(*) FROM duck)";
            return new PageSpec(base, count);
        }
        static PageSpec duck() {
            return typed(null);
        }
        static PageSpec flying() { return typed("FLYING"); }
        static PageSpec swimming() { return typed("SWIMMING"); }
        static PageSpec amphibious() { return typed("FLYING_AND_SWIMMING"); }
        private static PageSpec typed(String type) {
            // prepares query for ducks of given type (or all if type==null)
            String whereClause = type == null ? "" : " WHERE d.tip_rata = '" + type + "'";
            String sql = "SELECT ub.id, ub.username, ub.email, ub.password, NULL AS nume, NULL AS prenume, NULL AS ocupatie, NULL AS data_nasterii, NULL AS nivel_empatie, d.tip_rata, d.viteza, d.rezistenta " +
                    "FROM user_base ub JOIN duck d ON ub.id = d.id" + whereClause + " ORDER BY ub.id OFFSET ? LIMIT ?";
            String count = "SELECT COUNT(*) FROM duck" + (type == null ? "" : " WHERE tip_rata = '" + type + "'");
            return new PageSpec(sql, count);
        }
    }
}

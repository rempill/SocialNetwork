package repo;

import domain.Message;
import domain.User;
import errors.RepoError;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JDBC repository for messages.
 */
public class PostgresMessageRepository implements MessageRepository {
    private final String url;
    private final String user;
    private final String password;
    private final UserRepository userRepository;
    private Connection connection;

    public PostgresMessageRepository(String url, String user, String password, UserRepository userRepository) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.userRepository = userRepository;
        try {
            this.connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new RepoError("DB connection error: " + e.getMessage());
        }
    }

    @Override
    public Message findOne(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id null");
        }
        String sql = "SELECT sender_id, text, created_at, reply_to FROM messages WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User from = userRepository.findOne(rs.getInt("sender_id"));
                List<User> recipients = loadRecipients(id);
                Message reply = rs.getObject("reply_to") != null ? findOne(rs.getInt("reply_to")) : null;
                return new Message(id, from, recipients, rs.getString("text"),
                        rs.getTimestamp("created_at").toLocalDateTime(), reply);
            }
        } catch (SQLException e) {
            throw new RepoError("DB findOne error: " + e.getMessage());
        }
    }

    private List<User> loadRecipients(int messageId) throws SQLException {
        String sql = "SELECT recipient_id FROM message_recipients WHERE message_id = ?";
        List<User> recipients = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = userRepository.findOne(rs.getInt("recipient_id"));
                    if (u != null) {
                        recipients.add(u);
                    }
                }
            }
        }
        return recipients;
    }

    @Override
    public Iterable<Message> findAll() {
        throw new UnsupportedOperationException("Use conversation queries");
    }

    @Override
    public Message save(Message entity) {
        if (entity == null) throw new IllegalArgumentException("message null");
        String sql = "INSERT INTO messages(sender_id, text, created_at, reply_to) VALUES(?,?,?,?) RETURNING id";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, entity.getFrom().getId());
            ps.setString(2, entity.getMessage());
            ps.setTimestamp(3, Timestamp.valueOf(entity.getCreatedAt()));
            if (entity.getReplyTo() != null) {
                ps.setInt(4, entity.getReplyTo().getId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new RepoError("Failed to save message");
                }
                int id = rs.getInt(1);
                insertRecipients(id, entity.getTo());
                return new Message(id, entity.getFrom(), entity.getTo(), entity.getMessage(), entity.getCreatedAt(), entity.getReplyTo());
            }
        } catch (SQLException e) {
            throw new RepoError("DB save error: " + e.getMessage());
        }
    }

    @Override
    public Message delete(Integer id) {
        throw new UnsupportedOperationException("Delete not supported");
    }

    @Override
    public List<Message> findConversation(int userId, int otherUserId) {
        String sql = "SELECT m.id, m.sender_id, m.text, m.created_at, m.reply_to " +
                "FROM messages m " +
                "JOIN message_recipients r ON m.id = r.message_id " +
                "WHERE (m.sender_id = ? AND r.recipient_id = ?) OR (m.sender_id = ? AND r.recipient_id = ?) " +
                "ORDER BY m.created_at";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, otherUserId);
            ps.setInt(3, otherUserId);
            ps.setInt(4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Message> messages = new ArrayList<>();
                Map<Integer, Message> cache = new HashMap<>();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    User from = userRepository.findOne(rs.getInt("sender_id"));
                    List<User> recipients = loadRecipients(id);
                    Integer replyToId = rs.getObject("reply_to") != null ? rs.getInt("reply_to") : null;
                    Message reply = replyToId != null ? cache.computeIfAbsent(replyToId, this::findOne) : null;
                    Message m = new Message(id, from, recipients, rs.getString("text"),
                            rs.getTimestamp("created_at").toLocalDateTime(), reply);
                    cache.put(id, m);
                    messages.add(m);
                }
                return messages;
            }
        } catch (SQLException e) {
            throw new RepoError("DB conversation error: " + e.getMessage());
        }
    }

    @Override
    public Message saveReply(Message reply) {
        return save(reply);
    }

    private void insertRecipients(int messageId, List<User> recipients) throws SQLException {
        String sql = "INSERT INTO message_recipients(message_id, recipient_id) VALUES(?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (User to : recipients) {
                ps.setInt(1, messageId);
                ps.setInt(2, to.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}

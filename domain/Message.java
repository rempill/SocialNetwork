package domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a message exchanged between users. Supports multi-recipient and replies.
 */
public class Message {
    private final int id;
    private final User from;
    private final List<User> to;
    private final String message;
    private final LocalDateTime createdAt;
    private final Message replyTo;

    public Message(int id, User from, List<User> to, String message, LocalDateTime createdAt) {
        this(id, from, to, message, createdAt, null);
    }

    public Message(int id, User from, List<User> to, String message, LocalDateTime createdAt, Message replyTo) {
        if (from == null) throw new IllegalArgumentException("sender missing");
        if (to == null || to.isEmpty()) throw new IllegalArgumentException("recipients missing");
        if (message == null || message.isBlank()) throw new IllegalArgumentException("text missing");
        this.id = id;
        this.from = from;
        this.to = new ArrayList<>(to);
        this.message = message;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.replyTo = replyTo;
    }

    public int getId() {
        return id;
    }

    public User getFrom() {
        return from;
    }

    public List<User> getTo() {
        return Collections.unmodifiableList(to);
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Message getReplyTo() {
        return replyTo;
    }
}

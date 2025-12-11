package domain;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reply message referencing a parent message.
 */
public class ReplyMessage extends Message {
    public ReplyMessage(int id, User from, List<User> to, String message, LocalDateTime createdAt, Message replyTo) {
        super(id, from, to, message, createdAt, replyTo);
        if (replyTo == null) {
            throw new IllegalArgumentException("Reply target missing");
        }
    }
}

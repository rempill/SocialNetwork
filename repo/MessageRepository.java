package repo;

import domain.Message;

import java.util.List;

/**
 * Repository for messages and reply threads.
 */
public interface MessageRepository extends Repository<Integer, Message> {
    List<Message> findConversation(int userId, int otherUserId);
    Message saveReply(Message reply);
}


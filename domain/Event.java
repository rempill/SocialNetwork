package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * General event in the network (Observable). Users can subscribe/unsubscribe
 * and the event can notify them (simple console notification). Each event keeps
 * a list of subscribers and a notification log with all messages that were sent.
 */
public class Event {
    private final int id;
    private final String name;
    private final List<User> subscribers = new ArrayList<>();
    private final List<String> notificationLog = new ArrayList<>();

    /**
     * Construct a new event with id and name.
     *
     * @param id    event identifier
     * @param name  non-empty display name
     * @throws IllegalArgumentException if name is null or empty
     */
    public Event(int id, String name) {
        if(name == null || name.isEmpty()) throw new IllegalArgumentException("Event name must not be empty");
        this.id = id;
        this.name = name;
    }

    /**
     * @return the unique id of this event
     */
    public int getId() { return id; }

    /**
     * @return the display name of this event
     */
    public String getName() { return name; }

    /**
     * @return an unmodifiable view of the current subscribers
     */
    public List<User> getSubscribers() { return Collections.unmodifiableList(subscribers); }

    /**
     * @return an unmodifiable view of the notification messages sent by this event
     */
    public List<String> getNotificationLog() { return Collections.unmodifiableList(notificationLog); }

    /**
     * Subscribe a user to receive notifications from this event. Duplicates are ignored.
     *
     * @param user non-null user to subscribe
     */
    public void subscribe(User user) {
        if(user != null && !subscribers.contains(user)) {
            subscribers.add(user);
        }
    }

    /**
     * Unsubscribe a user from this event.
     *
     * @param user user to remove (no-op if not subscribed)
     */
    public void unsubscribe(User user) {
        subscribers.remove(user);
    }

    /**
     * Notify all subscribers with a message and append it to the local notification log.
     * Implemented as simple console output per subscriber for demo purposes.
     *
     * @param message message to broadcast
     */
    protected void notifySubscribers(String message) {
        String full = "[Event " + id + ":" + name + "] " + message;
        notificationLog.add(full);
        for (User u : subscribers) {
            // push notification into user's inbox
            u.receiveNotification(full);
            System.out.println("[Notify " + u.getUsername() + "] " + full);
        }
    }
}

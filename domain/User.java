package domain;
import java.util.ArrayList;
import java.util.List;

/**
 * Base abstract class that represents a user in the social network.
 * Concrete subclasses (e.g. Persoana, Duck) add domain-specific fields.
 */
public abstract class User {
    protected int id;
    protected String username,email,password;
    protected List<User> friends;

    /**
     * Create a new User.
     *
     * @param id unique identifier for the user
     * @param name username (display name)
     * @param email user's email address
     * @param password user's password
     */
    public User(int id, String name, String email, String password) {
        this.id = id;
        this.username = name;
        this.email = email;
        this.password = password;
        this.friends=new ArrayList<User>();
    }

    /**
     * Add a friend to this user's friend list if not already present and not the same user.
     *
     * @param user the user to add as a friend
     */
    public void addFriend(User user){
        if(!friends.contains(user) && !this.equals(user)){
            friends.add(user);
        }
    }

    /**
     * Get the user's id.
     *
     * @return numeric id
     */
    public int getId() {
        return id;
    }

    /**
     * Get the user's username.
     *
     * @return username string
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the user's email address.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Get the user's password (plain text in this simple example).
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get this user's friends list.
     *
     * @return mutable list of friends
     */
    public List<User> getFriends() {
        return friends;
    }

    /**
     * Remove a friend from this user's friend list.
     *
     * @param user the friend to remove
     */
    public void removeFriend(User user){
        friends.remove(user);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", friends=" + friends.size() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if(obj==null || getClass()!=obj.getClass()) return false;
        User other=(User) obj;
        return this.id==other.id;
    }
}

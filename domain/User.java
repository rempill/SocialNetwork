package domain;
import java.util.ArrayList;
import java.util.List;
public abstract class User {
    protected int id;
    protected String username,email,password;
    protected List<User> friends;

    public User(int id, String name, String email, String password) {
        this.id = id;
        this.username = name;
        this.email = email;
        this.password = password;
        this.friends=new ArrayList<User>();
    }

    public void addFriend(User user){
        if(!friends.contains(user) && !this.equals(user)){
            friends.add(user);
        }
    }

    public int getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
    public List<User> getFriends() {
        return friends;
    }

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

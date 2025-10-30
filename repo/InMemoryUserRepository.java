package repo;

import domain.User;
import errors.RepoError;

import java.util.Map;

public class InMemoryUserRepository implements UserRepository {
    private Map<Integer, User> storage;

    public InMemoryUserRepository(){
        this.storage = new java.util.HashMap<>();
    }
    public InMemoryUserRepository(Map<Integer, User> storage){
        this.storage = storage;
    }

    // CRUD operations
    @Override
    public User findOne(Integer id){
        if(id==null){
            throw new IllegalArgumentException("id is null");
        }
        return storage.get(id);
    }

    @Override
    public Iterable<User> findAll(){
        return storage.values();
    }

    @Override
    public User save(User user) throws RepoError {
        if(user==null){
            throw new IllegalArgumentException("user is null");
        }
        if(storage.containsKey(user.getId())){
            throw new RepoError("User with id " + user.getId() + " already exists.");
        }
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public User delete(Integer id){
        if(id==null){
            throw new IllegalArgumentException("id is null");
        }
        return storage.remove(id);
    }
}

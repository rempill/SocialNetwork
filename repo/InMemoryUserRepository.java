package repo;

import domain.User;
import errors.RepoError;

import java.util.Map;

/**
 * Simple in-memory repository implementation for {@link domain.User} objects.
 * This implementation uses a HashMap keyed by Integer id. It is intended for
 * testing and small examples.
 */
public class InMemoryUserRepository implements UserRepository {
    private Map<Integer, User> storage;

    /**
     * Create an empty in-memory repository.
     */
    public InMemoryUserRepository(){
        this.storage = new java.util.HashMap<>();
    }

    /**
     * Create a repository that uses the provided storage map. Useful for tests.
     *
     * @param storage map used as underlying storage
     */
    public InMemoryUserRepository(Map<Integer, User> storage){
        this.storage = storage;
    }

    // CRUD operations
    /**
     * Find a user by id.
     *
     * @param id user's id (must not be null)
     * @return the user or null if not found
     * @throws IllegalArgumentException when id is null
     */
    @Override
    public User findOne(Integer id){
        if(id==null){
            throw new IllegalArgumentException("id is null");
        }
        return storage.get(id);
    }

    /**
     * Return all users in the repository.
     *
     * @return iterable of users
     */
    @Override
    public Iterable<User> findAll(){
        return storage.values();
    }

    /**
     * Save a user in the repository. Throws {@link RepoError} if a user with the
     * same id already exists.
     *
     * @param user user to save (must not be null)
     * @return the saved user
     * @throws RepoError when a duplicate id is present
     * @throws IllegalArgumentException when user is null
     */
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

    /**
     * Delete a user by id.
     *
     * @param id id of the user to delete (must not be null)
     * @return the deleted user or null if not present
     * @throws IllegalArgumentException when id is null
     */
    @Override
    public User delete(Integer id){
        if(id==null){
            throw new IllegalArgumentException("id is null");
        }
        return storage.remove(id);
    }
}

package service;

import domain.Duck;
import domain.Persoana;
import domain.User;
import errors.RepoError;
import errors.ValidationError;
import repo.UserRepository;
import util.Algorithms;
import validator.ValidationStrategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service layer that orchestrates repository access, validation and business
 * operations for the social network domain.
 */
public class NetworkService {
    private UserRepository userRepository;
    private ValidationStrategy<Persoana> persoanaValidator;
    private ValidationStrategy<Duck> duckValidator;

    /**
     * Construct the NetworkService with required dependencies.
     *
     * @param userRepository repository for users
     * @param persoanaValidator validator for Persoana instances
     * @param duckValidator validator for Duck instances
     */
    public NetworkService(UserRepository userRepository, ValidationStrategy<Persoana> persoanaValidator, ValidationStrategy<Duck> duckValidator) {
        this.userRepository = userRepository;
        this.persoanaValidator = persoanaValidator;
        this.duckValidator = duckValidator;
    }

    /**
     * Add a new user to the network after validating it.
     *
     * @param user the user to add (Persoana or Duck)
     * @return the saved user
     * @throws ValidationError when validation fails
     * @throws RepoError when repository rejects the save (e.g. duplicate id)
     */
    public User addUser(User user) throws ValidationError, RepoError {
        if(user instanceof Persoana){
            persoanaValidator.validate((Persoana) user);
        }
        else if(user instanceof Duck){
            duckValidator.validate((Duck) user);
        }
        else{
            throw new ValidationError("Unknown user type");
        }
        return userRepository.save(user);
    }

    /**
     * Remove a user by id and clean up references from other users' friend lists.
     *
     * @param id the id of the user to remove
     * @return the removed user
     * @throws RepoError when the user does not exist
     */
    public User removeUser(Integer id){
        User userToRemove=userRepository.delete(id);
        if(userToRemove==null){
            throw new RepoError("User with id "+id+" not found");
        }
        for (User u: userRepository.findAll()) {
            u.removeFriend(userToRemove);
        }
        return userToRemove;
    }

    /**
     * Create a mutual friendship between two users identified by their ids.
     *
     * @param id1 first user's id
     * @param id2 second user's id
     * @throws RepoError when either user is not found
     */
    public void addFriendship(Integer id1, Integer id2){
        User user1=userRepository.findOne(id1);
        User user2=userRepository.findOne(id2);
        if(user1==null){
            throw new RepoError("User with id "+id1+" not found");
        }
        if(user2==null){
            throw new RepoError("User with id "+id2+" not found");
        }
        user1.addFriend(user2);
        user2.addFriend(user1);
    }

    /**
     * Remove a mutual friendship between two users (if present).
     *
     * @param id1 first user's id
     * @param id2 second user's id
     */
    public void removeFriendship(Integer id1, Integer id2){
        User user1=userRepository.findOne(id1);
        User user2=userRepository.findOne(id2);
        if(user1!=null){
            user1.removeFriend(user2);
        }
        if(user2!=null){
            user2.removeFriend(user1);
        }
    }

    /**
     * Retrieve all users in the network.
     *
     * @return iterable of all users
     */
    public Iterable<User> getAllUsers(){
        return userRepository.findAll();
    }

    /**
     * Compute the number of connected components (communities) in the network.
     *
     * @return number of communities
     */
    public int getNumberOfCommunities(){
        Set<User> visited=new HashSet<>();
        int communities=0;
        for (User user: userRepository.findAll()) {
            if(!visited.contains(user)){
                communities++;
                Algorithms.dfs(user, visited);
            }
        }
        return communities;
    }

    /**
     * Find the community with the largest diameter (most social community).
     * The method returns the list of users in the community with the maximum
     * shortest-path diameter.
     *
     * @return list of users forming the most social community
     */
    public List<User> getMostSocialCommunity() {
        Set<User> visitedGlobal = new HashSet<>();
        List<User> bestCommunity = new ArrayList<>();
        int maxDiameter = -1;

        // 1. Găsește fiecare comunitate (componentă conexă)
        for (User user : userRepository.findAll()) {
            if (!visitedGlobal.contains(user)) {
                Set<User> currentCommunitySet = new HashSet<>();
                Algorithms.dfs(user, currentCommunitySet); // Găsește toți membrii
                visitedGlobal.addAll(currentCommunitySet);

                List<User> currentCommunityList = new ArrayList<>(currentCommunitySet);

                // 2. Calculează diametrul (cel mai lung drum scurt)
                int currentDiameter = Algorithms.getDiameter(currentCommunityList);

                // 3. Compară
                if (currentDiameter > maxDiameter) {
                    maxDiameter = currentDiameter;
                    bestCommunity = currentCommunityList;
                }
            }
        }
        System.out.println("Diametrul maxim găsit: " + maxDiameter);
        return bestCommunity;
    }
}

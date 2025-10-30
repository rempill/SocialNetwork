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

public class NetworkService {
    private UserRepository userRepository;
    private ValidationStrategy<Persoana> persoanaValidator;
    private ValidationStrategy<Duck> duckValidator;

    public NetworkService(UserRepository userRepository, ValidationStrategy<Persoana> persoanaValidator, ValidationStrategy<Duck> duckValidator) {
        this.userRepository = userRepository;
        this.persoanaValidator = persoanaValidator;
        this.duckValidator = duckValidator;
    }

    // adds a new user to the network
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

    // removes a user from the network and also removes him from all his friends' friend lists
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

    // adds a friendship between two users
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

    // removes the friendship between two users
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

    // retrieves all users in the network
    public Iterable<User> getAllUsers(){
        return userRepository.findAll();
    }

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

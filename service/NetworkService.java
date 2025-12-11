package service;

import domain.*;
import errors.RepoError;
import errors.ValidationError;
import repo.EventRepository;
import repo.UserRepository;
import repo.PostgresUserRepository; // added
import repo.PostgresEventRepository;
import repo.CardRepository;
import repo.MessageRepository;
import util.PageResult;
import util.Algorithms;
import util.PasswordHasher;
import validator.ValidationStrategy;

import java.util.*;

/**
 * Service layer that orchestrates repository access, validation and business
 * operations for the social network domain.
 */
public class NetworkService {
    private UserRepository userRepository;
    private EventRepository eventRepository;
    private ValidationStrategy<Persoana> persoanaValidator;
    private ValidationStrategy<Duck> duckValidator;
    private final Map<Integer, Card> cards = new HashMap<>();
    private final CardRepository cardRepository;
    private final MessageRepository messageRepository;

    /**
     * Construct the NetworkService with required dependencies.
     *
     * @param userRepository repository for users
     * @param eventRepository repository for events
     * @param persoanaValidator validator for Persoana instances
     * @param duckValidator validator for Duck instances
     * @param cardRepository repository for cards
     * @param messageRepository repository for messages
     */
    public NetworkService(UserRepository userRepository, EventRepository eventRepository, ValidationStrategy<Persoana> persoanaValidator, ValidationStrategy<Duck> duckValidator, CardRepository cardRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.persoanaValidator = persoanaValidator;
        this.duckValidator = duckValidator;
        this.cardRepository = cardRepository;
        this.messageRepository = messageRepository;
        if(cardRepository != null){
            for (Card card : cardRepository.findAll()) {
                cards.put(card.getId(), card);
            }
        }
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
        User toPersist = hashPassword(user);
        return userRepository.save(toPersist);
    }

    private User hashPassword(User user) {
        user.setPassword(PasswordHasher.hash(user.getPassword()));
        return user;
    }

    public User login(String email, String password) {
        User found = userRepository.findByEmail(email == null ? null : email.toLowerCase());
        if (found == null) {
            throw new RepoError("Invalid credentials");
        }
        String stored = found.getPassword();
        boolean isHashed = stored != null && stored.startsWith("$2");
        boolean authenticated;
        if (isHashed) {
            authenticated = PasswordHasher.matches(password, stored);
        } else {
            authenticated = Objects.equals(password, stored);
            if (authenticated) {
                String newHash = PasswordHasher.hash(password);
                userRepository.updatePassword(found.getId(), newHash);
                found.setPassword(newHash);
            }
        }
        if (!authenticated) {
            throw new RepoError("Invalid credentials");
        }
        return reloadUser(found.getId());
    }

    /**
     * Remove a user by id and clean up references from other users' friend lists.
     * Also removes a duck from all cards where it appears.
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
        // also remove from cards if it's a Duck
        if (userToRemove instanceof Duck) {
            Duck d = (Duck) userToRemove;
            for (Card c : cards.values()) {
                c.removeDuck(d);
            }
            if (cardRepository != null) {
                cardRepository.removeDuckFromAll(d.getId());
            }
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
        if(userRepository instanceof PostgresUserRepository pr){
            pr.saveFriendship(id1, id2);
        }
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
        if(userRepository instanceof PostgresUserRepository pr){
            pr.deleteFriendship(id1, id2);
        }
    }

    /**
     * Retrieve all users from the repository, optionally filtering by user type.
     *
     * @param userType optional class type to filter users (e.g., Persoana.class or Duck.class)
     * @return list of users matching the filter
     */
    public List<User> getAllUsers(Class<? extends User> userType){
        List<User> result = new ArrayList<>();
        for (User u : userRepository.findAll()) {
            if (userType == null || userType.isInstance(u)) {
                result.add(u);
            }
        }
        return result;
    }

    public PageResult<Duck> getDucksPage(int pageIndex, int pageSize, TipRata filter) {
        return userRepository.findDuckPage(pageIndex, pageSize, filter);
    }

    public Iterable<Duck> getAllDucks() {
        return userRepository.findAllDucks();
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
        for (User user : userRepository.findAll()) {
            if (!visitedGlobal.contains(user)) {
                Set<User> currentCommunitySet = new HashSet<>();
                Algorithms.dfs(user, currentCommunitySet); // Găsește toți membrii
                visitedGlobal.addAll(currentCommunitySet);
                List<User> currentCommunityList = new ArrayList<>(currentCommunitySet);
                int currentDiameter = Algorithms.getDiameter(currentCommunityList);
                if (currentDiameter > maxDiameter) {
                    maxDiameter = currentDiameter;
                    bestCommunity = currentCommunityList;
                }
            }
        }
        System.out.println("Diametrul maxim găsit: " + maxDiameter);
        return bestCommunity;
    }

    // CARD operations
    /**
     * Create a card (flock) that can contain ducks and compute their average performance.
     *
     * @param numeCard card name
     * @return created {@link Card}
     */
    public Card createCard(String numeCard){
        Card persisted;
        if (cardRepository != null) {
            // persist first to obtain DB-generated id
            persisted = cardRepository.save(new Card(-1, numeCard));
        } else {
            // fallback to in-memory id assignment
            int newId = cards.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
            persisted = new Card(newId, numeCard);
        }
        cards.put(persisted.getId(), persisted);
        return persisted;
    }

    /**
     * @return view of all created cards
     */
    public Collection<Card> getAllCards(){
        return cards.values();
    }

    /**
     * Add a duck to a card by their ids.
     *
     * @param cardId card id
     * @param duckId duck user id
     */
    public void addDuckToCard(int cardId, int duckId){
        Card c = cards.get(cardId);
        if(c==null) throw new RepoError("Card not found");
        User u = userRepository.findOne(duckId);
        if(!(u instanceof Duck)) throw new RepoError("User is not a duck or not found");
        c.addDuck((Duck) u);
        if(cardRepository != null) {
            cardRepository.addDuck(cardId, duckId);
        }
    }

    /**
     * Remove a duck from a card by their ids.
     *
     * @param cardId card id
     * @param duckId duck user id
     */
    public void removeDuckFromCard(int cardId, int duckId){
        Card c = cards.get(cardId);
        if(c==null) throw new RepoError("Card not found");
        User u = userRepository.findOne(duckId);
        if(!(u instanceof Duck)) throw new RepoError("User is not a duck or not found");
        c.removeDuck((Duck) u);
        if(cardRepository != null) {
            cardRepository.removeDuck(cardId, duckId);
        }
    }

    /**
     * Compute the average performance value for a card.
     *
     * @param cardId card id
     * @return average performance
     */
    public double getCardPerformantaMedie(int cardId){
        Card c = cards.get(cardId);
        if(c==null) throw new RepoError("Card not found");
        return c.getPerformantaMedie();
    }

    // EVENT operations using repository
    /**
     * Create and store a race event with a specific lane count.
     *
     * @param name  event name
     * @param lanes number of lanes (M)
     * @return created {@link RaceEvent} with a DB-generated id
     */
    public RaceEvent createRaceEvent(String name, int lanes){
        // temporary id; PostgresEventRepository should ignore/use its own sequence
        RaceEvent re = new RaceEvent(-1, name, lanes);
        eventRepository.save(re);
        return re;
    }

    /**
     * @return all events stored in the repository
     */
    public Iterable<Event> getAllEvents(){ return eventRepository.findAll(); }

    /**
     * Subscribe a user to an event by ids.
     *
     * @param eventId event id
     * @param userId  user id
     */
    public void subscribeToEvent(int eventId, int userId){
        Event e = eventRepository.findOne(eventId);
        if(e==null) throw new RepoError("Event not found");
        User u = userRepository.findOne(userId);
        if(u==null) throw new RepoError("User not found");
        e.subscribe(u);
        if(eventRepository instanceof PostgresEventRepository pe){
            pe.addSubscriber(eventId, userId);
        }
    }

    /**
     * Unsubscribe a user from an event (no-op if user or event does not exist).
     *
     * @param eventId event id
     * @param userId  user id
     */
    public void unsubscribeFromEvent(int eventId, int userId){
        Event e = eventRepository.findOne(eventId);
        if(e==null) throw new RepoError("Event not found");
        User u = userRepository.findOne(userId);
        if(u!=null) {
            e.unsubscribe(u);
            if(eventRepository instanceof PostgresEventRepository pe){
                pe.removeSubscriber(eventId, userId);
            }
        }
    }

    /**
     * Run a race event identified by id. It gathers all users that are both Duck and Inotator,
     * selects up to M participants, runs the optimizer and returns a textual report.
     *
     * @param eventId the race event id
     * @return list of report lines (one per lane + minimal total time)
     */
    public List<String> runRace(int eventId){
        Event ev = eventRepository.findOne(eventId);
        if(!(ev instanceof RaceEvent)) throw new RepoError("Event is not a race");
        RaceEvent re = (RaceEvent) ev;
        List<Duck> allDucks = new ArrayList<>();
        for (Duck duck : userRepository.findAllDucks()) {
            if (duck instanceof Inotator) {
                allDucks.add(duck);
            }
        }
        re.selectParticipants(allDucks);
        List<String> report = re.runRaceAndReport();
        if(eventRepository instanceof PostgresEventRepository pe){
            pe.addNotification(eventId, "Race finished. Results available.");
        }
        return report;
    }

    /**
     * Configure lane distances for a race event.
     *
     * @param eventId   race event id
     * @param distances per-lane distances (exactly M values)
     */
    public void setRaceDistances(int eventId, double[] distances){
        Event ev = eventRepository.findOne(eventId);
        if(!(ev instanceof RaceEvent)) throw new errors.RepoError("Event is not a race");
        ((RaceEvent) ev).setDistances(distances);
    }

    /**
     * Get the notifications received by a specific user.
     *
     * @param userId id of the user
     * @return unmodifiable list of messages
     * @throws RepoError if the user is not found
     */
    public List<String> getUserNotifications(int userId){
        User u = userRepository.findOne(userId);
        if(u==null) throw new RepoError("User not found");
        return u.getNotifications();
    }

    public void addFriendshipByEmail(String emailA, String emailB) {
        User a = requireUserByEmail(emailA);
        User b = requireUserByEmail(emailB);
        addFriendship(a.getId(), b.getId());
    }

    public void removeFriendshipByEmail(String emailA, String emailB) {
        User a = requireUserByEmail(emailA);
        User b = requireUserByEmail(emailB);
        removeFriendship(a.getId(), b.getId());
    }

    public User requireUserByEmail(String email) {
        User user = userRepository.findByEmail(email == null ? null : email.toLowerCase());
        if (user == null) {
            throw new RepoError("User not found for email " + email);
        }
        return user;
    }

    public User getUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        return userRepository.findByEmail(email.toLowerCase());
    }

    public User reloadUser(int userId) {
        for (User user : userRepository.findAll()) {
            if (user.getId() == userId) {
                return user;
            }
        }
        return null;
    }

    public List<User> getFriendsFor(int userId) {
        User user = reloadUser(userId);
        if (user == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(user.getFriends());
    }

    public Message sendMessage(User from, List<User> to, String text) {
        Message message = new Message(-1, from, to, text, java.time.LocalDateTime.now());
        return messageRepository.save(message);
    }

    public Message replyMessage(User from, List<User> to, String text, Message replyTo) {
        Message reply = new Message(-1, from, to, text, java.time.LocalDateTime.now(), replyTo);
        return messageRepository.saveReply(reply);
    }

    public List<Message> getConversation(int userId, int otherUserId) {
        return messageRepository.findConversation(userId, otherUserId);
    }
}

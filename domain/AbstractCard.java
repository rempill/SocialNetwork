package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generic, immutable-identity card (flock) that groups ducks of a specific type.
 * Provides read-only accessors for metadata and members, mutation helpers to add/remove
 * ducks, and convenience metrics such as average speed/endurance and overall performance.
 *
 * @param <T> concrete duck subtype allowed in this card
 */
public abstract class AbstractCard<T extends Duck> {
    protected final int id;
    protected final String numeCard;
    protected final List<T> membri = new ArrayList<>();

    /**
     * Create a new card with id and name.
     *
     * @param id       unique identifier
     * @param numeCard display name of the card
     */
    protected AbstractCard(int id, String numeCard) {
        this.id = id;
        this.numeCard = numeCard;
    }

    /**
     * @return unique identifier of the card
     */
    public int getId() { return id; }

    /**
     * @return card display name
     */
    public String getNumeCard() { return numeCard; }

    /**
     * @return unmodifiable view of current duck members
     */
    public List<T> getMembri() { return Collections.unmodifiableList(membri); }

    /**
     * Add a duck to the card (no duplicates).
     *
     * @param d duck to add
     * @return true if added, false if null or already present
     */
    public boolean addDuck(T d) { return d != null && !membri.contains(d) && membri.add(d); }

    /**
     * Remove a duck from the card.
     *
     * @param d duck to remove
     * @return true if removed, false otherwise
     */
    public boolean removeDuck(T d) { return membri.remove(d); }

    /**
     * Compute average of the speed attribute across members.
     *
     * @return average speed (0.0 if no members)
     */
    public double getAverageSpeed() {
        if (membri.isEmpty()) return 0.0;
        double sum = 0.0;
        for (T d : membri) sum += d.getViteza();
        return sum / membri.size();
    }

    /**
     * Compute average of the endurance attribute across members.
     *
     * @return average endurance (0.0 if no members)
     */
    public double getAverageRezistenta() {
        if (membri.isEmpty()) return 0.0;
        double sum = 0.0;
        for (T d : membri) sum += d.getRezistenta();
        return sum / membri.size();
    }

    /**
     * Compute overall performance as the mean of average speed and average endurance.
     *
     * @return performance score in the same units as the attributes
     */
    public double getPerformantaMedie() { return (getAverageSpeed() + getAverageRezistenta()) / 2.0; }
}

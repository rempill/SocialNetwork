package domain;

/**
 * Card (flock) of ducks with a common goal (e.g., SwimMasters, SkyFlyers).
 * Holds an id, a name and a list of Duck members and can compute average performance.
 */
public class Card extends AbstractCard<Duck> {
    /**
     * Construct a card with id and display name.
     *
     * @param id       unique identifier
     * @param numeCard display name
     */
    public Card(int id, String numeCard) {
        super(id, numeCard);
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", nume='" + numeCard + '\'' +
                ", membri=" + membri.size() +
                '}';
    }
}

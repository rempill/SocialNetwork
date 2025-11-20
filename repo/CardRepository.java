package repo;

import domain.Card;

/**
 * Repository specialization for {@link Card} aggregates.
 */
public interface CardRepository extends Repository<Integer, Card> {
    /**
     * Persist the association between a card and a duck member.
     */
    void addDuck(int cardId, int duckId);

    /**
     * Remove the association between a card and a duck member.
     */
    void removeDuck(int cardId, int duckId);

    /**
     * Remove the association between all cards and a duck member.
     */
    void removeDuckFromAll(int duckId);
}

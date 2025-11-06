package repo;

import domain.Event;
import errors.RepoError;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple in-memory repository implementation for {@link domain.Event} objects.
 * Backed by a {@link java.util.HashMap} keyed by the event id. Intended for tests
 * and small demos.
 */
public class InMemoryEventRepository implements EventRepository {
    private final Map<Integer, Event> storage = new HashMap<>();

    /**
     * Find an event by id.
     *
     * @param id event id (must not be null)
     * @return the event or null if not found
     * @throws IllegalArgumentException if id is null
     */
    @Override
    public Event findOne(Integer id) {
        if(id == null) throw new IllegalArgumentException("id is null");
        return storage.get(id);
    }

    /**
     * Return all stored events.
     *
     * @return iterable view of all events
     */
    @Override
    public Iterable<Event> findAll() {
        return storage.values();
    }

    /**
     * Save an event. Throws if an event with the same id already exists.
     *
     * @param entity event to save (must not be null)
     * @return the saved event
     * @throws RepoError when a duplicate id is present
     * @throws IllegalArgumentException if entity is null
     */
    @Override
    public Event save(Event entity) throws RepoError {
        if(entity == null) throw new IllegalArgumentException("entity is null");
        if(storage.containsKey(entity.getId())) throw new RepoError("Event with id "+entity.getId()+" already exists.");
        storage.put(entity.getId(), entity);
        return entity;
    }

    /**
     * Delete an event by id.
     *
     * @param id id of the event to delete (must not be null)
     * @return the removed event or null if not found
     * @throws IllegalArgumentException if id is null
     */
    @Override
    public Event delete(Integer id) {
        if(id == null) throw new IllegalArgumentException("id is null");
        return storage.remove(id);
    }
}

package repo;

import errors.RepoError;

/**
 * Generic repository interface describing basic CRUD operations.
 *
 * @param <ID> identifier type
 * @param <E>  entity type
 */
public interface Repository<ID,E>{
    /**
     * Find an entity by its id.
     *
     * @param id the entity identifier
     * @return the entity or null if not found
     */
    E findOne(ID id);

    /**
     * Return all entities in the repository.
     *
     * @return iterable over all entities
     */
    Iterable<E> findAll();

    /**
     * Save the given entity in the repository.
     * Implementations may throw {@link RepoError} for domain-specific errors
     * (e.g. duplicates).
     *
     * @param entity the entity to save
     * @return the saved entity
     * @throws RepoError when saving fails for repository-specific reasons
     */
    E save(E entity) throws RepoError;

    /**
     * Delete an entity by id.
     *
     * @param id the identifier of the entity to delete
     * @return the deleted entity or null if it did not exist
     */
    E delete(ID id);
}

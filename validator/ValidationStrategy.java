package validator;

import errors.ValidationError;

/**
 * Strategy interface for validating entities of type T.
 * Implementations should throw {@link errors.ValidationError} when validation fails.
 *
 * @param <T> the entity type to validate
 */
public interface ValidationStrategy<T> {
    /**
     * Validate the provided entity.
     * Implementations must throw a {@link ValidationError} describing validation issues when
     * the entity is invalid.
     *
     * @param entity the entity to validate
     * @throws ValidationError when validation fails
     */
    void validate(T entity) throws ValidationError;
}

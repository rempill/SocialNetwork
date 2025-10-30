package validator;

import errors.ValidationError;

public interface ValidationStrategy<T> {
    void validate(T entity) throws ValidationError;
}

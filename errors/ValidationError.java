package errors;

/**
 * Runtime exception thrown when validation of an entity fails.
 * The message should contain a human-readable description of all validation problems.
 */
public class ValidationError extends RuntimeException {
    /**
     * Create a validation error with the given message.
     *
     * @param message descriptive error message
     */
    public ValidationError(String message) {
        super(message);
    }
}

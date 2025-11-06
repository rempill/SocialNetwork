package errors;

/**
 * Runtime exception thrown by the service layer to indicate business logic or
 * orchestration problems (validation errors are represented by {@link ValidationError}).
 */
public class ServiceError extends RuntimeException {
    /**
     * Create a service error with the given message.
     *
     * @param message descriptive error message
     */
    public ServiceError(String message) {
        super(message);
    }
}

package errors;

/**
 * Runtime exception thrown when repository operations fail (e.g. entity not found,
 * duplicate insertions, etc.).
 */
public class RepoError extends RuntimeException {
    /**
     * Create a repository error with the given message.
     *
     * @param message descriptive error message
     */
    public RepoError(String message) {
        super(message);
    }
}

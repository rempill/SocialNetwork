package util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Simple wrapper around BCrypt to hash and verify passwords.
 */
public final class PasswordHasher {
    private PasswordHasher() {}

    /**
     * Hash a plain text password with BCrypt.
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("password is null");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Validate a plain password against a previously hashed value.
     */
    public static boolean matches(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}


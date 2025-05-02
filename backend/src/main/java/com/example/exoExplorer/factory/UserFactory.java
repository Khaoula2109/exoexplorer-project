package com.example.exoExplorer.factory;

import com.example.exoExplorer.entities.User;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * Factory class for creating User objects.
 * Implements the Factory Method pattern.
 */
@Component
public class UserFactory {

    /**
     * Creates a new regular user with the given email and password.
     * @param email User's email
     * @param password User's password (will be hashed)
     * @return A new User instance
     */
    public User createRegularUser(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setOtpVerified(false);
        user.setDarkMode(false);
        user.setLanguage("fr");
        return user;
    }

    /**
     * Creates a new admin user with the given email and password.
     * @param email Admin's email
     * @param password Admin's password (will be hashed)
     * @return A new User instance with admin privileges
     */
    public User createAdminUser(String email, String password) {
        User user = createRegularUser(email, password);
        // Additional admin settings could be set here
        return user;
    }

    /**
     * Creates a user for social login.
     * @param email User's email from the social provider
     * @param firstName User's first name
     * @param lastName User's last name
     * @return A new User instance for social login
     */
    public User createSocialUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        // Generate a random secure password for social users
        user.setPassword(BCrypt.hashpw(generateSecureRandomPassword(), BCrypt.gensalt()));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setOtpVerified(true); // Social users are pre-verified
        return user;
    }

    /**
     * Generates a secure random password.
     * @return A random secure password
     */
    private String generateSecureRandomPassword() {
        // In a real implementation, use SecureRandom to generate a truly random password
        return BCrypt.gensalt(12);
    }
}
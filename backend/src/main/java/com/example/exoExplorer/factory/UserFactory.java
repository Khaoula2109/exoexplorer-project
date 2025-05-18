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
}
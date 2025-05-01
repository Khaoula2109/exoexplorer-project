package com.example.exoExplorer.controllers;

import com.example.exoExplorer.entities.Exoplanet;
import com.example.exoExplorer.entities.User;
import com.example.exoExplorer.exceptions.MissingEmailException;
import com.example.exoExplorer.exceptions.UserNotFoundException;
import com.example.exoExplorer.repositories.UserRepository;
import com.example.exoExplorer.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for user-related endpoints.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Gets a user's favorite exoplanets.
     *
     * @param email The user's email
     * @return List of favorite exoplanets
     */
    @GetMapping("/favorites")
    public ResponseEntity<List<Exoplanet>> getUserFavorites(@RequestParam(required = false) String email,
                                                            Authentication authentication) {
        if (email == null || email.isBlank()) {
            // Use authenticated user if email not provided
            if (authentication != null) {
                email = authentication.getName();
            } else {
                throw new MissingEmailException("Email requis pour récupérer les favoris");
            }
        }

        logger.info("Getting favorites for user: {}", email);
        List<Exoplanet> favorites = userService.getFavorites(email);
        return ResponseEntity.ok(favorites);
    }

    /**
     * Toggles favorite status of an exoplanet.
     *
     * @param payload The request payload containing email and exoplanet ID
     * @return Response indicating success
     */
    @PostMapping("/toggle-favorite")
    public ResponseEntity<Map<String, String>> toggleFavorite(@RequestBody Map<String, Object> payload,
                                                              Authentication authentication) {
        String email = (String) payload.get("email");
        Integer exoplanetId = (Integer) payload.get("exoplanetId");

        if (email == null || email.isBlank()) {
            // Use authenticated user if email not provided
            if (authentication != null) {
                email = authentication.getName();
            } else {
                throw new MissingEmailException("Email requis pour modifier les favoris");
            }
        }

        if (exoplanetId == null) {
            throw new MissingEmailException("ID exoplanète requis");
        }

        logger.info("Toggling favorite status of exoplanet {} for user: {}", exoplanetId, email);
        userService.toggleFavorite(email, exoplanetId);
        return ResponseEntity.ok(Map.of("message", "Favorite toggled successfully"));
    }

    /**
     * Gets a user's profile information.
     *
     * @param email The user's email
     * @return The user's profile information
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestParam String email) {
        logger.info("Getting profile for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        Map<String, Object> result = new HashMap<>();
        result.put("email", user.getEmail());
        result.put("firstName", user.getFirstName());
        result.put("lastName", user.getLastName());
        result.put("darkMode", user.isDarkMode());
        result.put("language", user.getLanguage());
        result.put("isAdmin", user.isAdmin());

        return ResponseEntity.ok(result);
    }

    /**
     * Updates a user's profile.
     *
     * @param payload The request payload containing profile information
     * @return Response indicating success
     */
    @PutMapping("/update-profile")
    public ResponseEntity<Map<String, String>> updateProfile(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String firstName = payload.get("firstName");
        String lastName = payload.get("lastName");

        if (email == null || email.isBlank()) {
            throw new MissingEmailException("Email requis pour mettre à jour le profil");
        }

        logger.info("Updating profile for user: {}", email);
        userService.updateUserProfile(email, firstName, lastName);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    /**
     * Changes a user's password.
     *
     * @param payload The request payload containing password information
     * @return Response indicating success
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String currentPassword = payload.get("currentPassword");
        String newPassword = payload.get("newPassword");

        if (email == null || currentPassword == null || newPassword == null) {
            throw new MissingEmailException("Tous les champs sont requis pour changer le mot de passe");
        }

        logger.info("Changing password for user: {}", email);
        userService.changePassword(email, currentPassword, newPassword);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * Updates a user's preferences.
     *
     * @param payload The request payload containing preferences
     * @return Response indicating success
     */
    @PutMapping("/preferences")
    public ResponseEntity<Map<String, String>> updatePreferences(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        Boolean darkMode = (Boolean) payload.get("darkMode");
        String language = (String) payload.get("language");

        if (email == null || email.isBlank()) {
            throw new MissingEmailException("Email requis pour mettre à jour les préférences");
        }

        logger.info("Updating preferences for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        if (darkMode != null) {
            user.setDarkMode(darkMode);
        }

        if (language != null) {
            user.setLanguage(language);
        }

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Preferences updated successfully"));
    }

    /**
     * Gets a user's backup codes.
     * Admin only endpoint.
     *
     * @param email The user's email
     * @return List of used and unused backup codes
     */
    @GetMapping("/backup-codes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBackupCodes(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        int total = user.getBackupCodes().size();
        long used = user.getBackupCodes().stream().filter(code -> code.getUsed()).count();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("used", used);
        result.put("available", total - used);

        return ResponseEntity.ok(result);
    }
}
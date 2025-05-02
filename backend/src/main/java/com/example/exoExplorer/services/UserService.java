package com.example.exoExplorer.services;

import com.example.exoExplorer.entities.Exoplanet;
import com.example.exoExplorer.entities.TwoFactorBackupCode;
import com.example.exoExplorer.entities.User;
import com.example.exoExplorer.exceptions.*;
import com.example.exoExplorer.factory.UserFactory;
import com.example.exoExplorer.observer.UserActionEvent;
import com.example.exoExplorer.observer.UserActionSubject;
import com.example.exoExplorer.repositories.ExoplaneteRepository;
import com.example.exoExplorer.repositories.UserRepository;
import com.example.exoExplorer.strategy.OtpStrategyFactory;
import com.example.exoExplorer.strategy.OtpVerificationStrategy;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing user-related operations.
 * Incorporates Factory, Strategy, and Observer patterns.
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExoplaneteRepository exoplanetRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserFactory userFactory;

    @Autowired
    private OtpStrategyFactory otpStrategyFactory;

    @Autowired
    private UserActionSubject userActionSubject;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Registers a new user.
     *
     * @param email User's email
     * @param password User's password
     * @return The newly created user
     * @throws UserAlreadyExistsException If a user with the same email already exists
     */
    @Transactional
    public User registerUser(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            logger.warn("Registration attempt for existing email: {}", email);
            throw new UserAlreadyExistsException("Cet utilisateur existe déjà");
        }

        // Use the factory to create the user
        User user = userFactory.createRegularUser(email, password);
        User savedUser = userRepository.save(user);

        // Notify observers of the registration
        userActionSubject.notifyObservers(UserActionEvent.USER_REGISTERED, user, null);

        return savedUser;
    }

    /**
     * Processes login by generating and sending an OTP.
     *
     * @param email User's email
     * @param password User's password
     * @throws UserNotFoundException If the user is not found
     * @throws BadCredentialsException If the password is incorrect
     */
    @Transactional
    public void processLogin(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        if (!BCrypt.checkpw(password, user.getPassword())) {
            logger.warn("Failed login attempt for user: {}", email);
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        // Generate OTP
        int otpInt = secureRandom.nextInt(900000) + 100000;
        String otp = String.valueOf(otpInt);
        String otpHash = BCrypt.hashpw(otp, BCrypt.gensalt());

        // Update user with OTP info
        user.setOtpCodeHash(otpHash);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpirationMinutes));
        user.setOtpVerified(false);
        userRepository.save(user);

        // Send OTP via email
        mailService.sendOtpEmail(email, otp);

        // Notify observers of the login attempt
        userActionSubject.notifyObservers(UserActionEvent.USER_LOGGED_IN, user, null);

        logger.info("OTP generated and sent for user: {}", email);
    }

    /**
     * Verifies an OTP for a user.
     *
     * @param email User's email
     * @param otp   The OTP to verify
     * @return
     * @throws UserNotFoundException If the user is not found
     * @throws InvalidOtpException   If the OTP is invalid or expired
     */
    @Transactional
    public User verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        // Use strategy pattern to verify OTP
        OtpVerificationStrategy strategy = otpStrategyFactory.getStrategy(false);
        strategy.verify(user, otp);

        // Update user after successful verification
        user.setOtpVerified(true);
        user.setOtpCodeHash(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        logger.info("OTP verified successfully for user: {}", email);
        return user;
    }

    /**
     * Generates backup codes for a user.
     *
     * @param email User's email
     * @param count Number of backup codes to generate
     * @return List of plain text backup codes
     * @throws UserNotFoundException If the user is not found
     */
    @Transactional
    public List<String> generateBackupCodes(String email, int count) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        user.getBackupCodes().clear();
        List<String> plainCodes = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int codeInt = secureRandom.nextInt(90000000) + 10000000;
            String plainCode = String.valueOf(codeInt);
            String codeHash = BCrypt.hashpw(plainCode, BCrypt.gensalt());

            TwoFactorBackupCode backup = new TwoFactorBackupCode(null, codeHash, false, user);
            user.getBackupCodes().add(backup);
            plainCodes.add(plainCode);
        }

        userRepository.save(user);
        logger.info("Generated {} backup codes for user: {}", count, email);

        return plainCodes;
    }

    /**
     * Verifies a backup code for a user.
     *
     * @param email User's email
     * @param backupCodeInput The backup code to verify
     * @return True if the code was verified, false otherwise
     * @throws UserNotFoundException If the user is not found
     */
    @Transactional
    public boolean verifyBackupCode(String email, String backupCodeInput) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        try {
            // Use strategy pattern to verify backup code
            OtpVerificationStrategy strategy = otpStrategyFactory.getStrategy(true);
            strategy.verify(user, backupCodeInput);

            // Save changes to backup codes
            userRepository.save(user);
            return true;
        } catch (InvalidOtpException e) {
            logger.warn("Invalid backup code used for user: {}", email);
            return false;
        }
    }

    /**
     * Toggles favorite status of an exoplanet for a user.
     *
     * @param email User's email
     * @param exoplanetId ID of the exoplanet to toggle
     * @throws UserNotFoundException If the user is not found
     * @throws ResourceNotFoundException If the exoplanet is not found
     */
    @Transactional
    @CacheEvict(value = "userFavorites", key = "#email")
    public void toggleFavorite(String email, Integer exoplanetId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        Exoplanet exo = exoplanetRepository.findById(exoplanetId)
                .orElseThrow(() -> new ResourceNotFoundException("Exoplanète introuvable"));

        boolean isAdding = !user.getFavorites().contains(exo);
        if (isAdding) {
            user.getFavorites().add(exo);
            userActionSubject.notifyObservers(UserActionEvent.USER_FAVORITE_ADDED, user, exo);
        } else {
            user.getFavorites().remove(exo);
            userActionSubject.notifyObservers(UserActionEvent.USER_FAVORITE_REMOVED, user, exo);
        }

        userRepository.save(user);
        logger.info("User {} {} exoplanet {} to favorites",
                email, isAdding ? "added" : "removed", exo.getName());
    }

    /**
     * Gets the favorite exoplanets for a user.
     *
     * @param email User's email
     * @return List of favorite exoplanets
     * @throws MissingEmailException If the email is null or empty
     * @throws UserNotFoundException If the user is not found
     */
    @Cacheable(value = "userFavorites", key = "#email")
    public List<Exoplanet> getFavorites(String email) {
        if (email == null || email.isBlank()) {
            throw new MissingEmailException("L'email est requis pour afficher les favoris");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        return new ArrayList<>(user.getFavorites());
    }

    /**
     * Updates a user's profile.
     *
     * @param email User's email
     * @param firstName User's first name
     * @param lastName User's last name
     * @throws MissingEmailException If the email is null or empty
     * @throws UserNotFoundException If the user is not found
     */
    @Transactional
    public void updateUserProfile(String email, String firstName, String lastName) {
        if (email == null || email.isBlank()) {
            throw new MissingEmailException("L'email est requis pour mettre à jour le profil");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        user.setFirstName(firstName);
        user.setLastName(lastName);
        userRepository.save(user);

        userActionSubject.notifyObservers(UserActionEvent.PROFILE_UPDATED, user, null);
        logger.info("Profile updated for user: {}", email);
    }

    /**
     * Changes a user's password.
     *
     * @param email User's email
     * @param currentPassword User's current password
     * @param newPassword User's new password
     * @throws UserNotFoundException If the user is not found
     * @throws BadCredentialsException If the current password is incorrect
     */
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur introuvable"));

        if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
            logger.warn("Failed password change attempt for user: {}", email);
            throw new BadCredentialsException("Mot de passe actuel incorrect");
        }

        user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        userRepository.save(user);

        userActionSubject.notifyObservers(UserActionEvent.PASSWORD_CHANGED, user, null);
        logger.info("Password changed for user: {}", email);
    }
}
package com.example.exoExplorer.observer;

import com.example.exoExplorer.entities.Exoplanet;
import com.example.exoExplorer.entities.User;
import com.example.exoExplorer.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Notification observer for user actions.
 * Sends notifications for important user actions.
 */
@Component
public class NotificationUserActionObserver implements UserActionObserver {
    private static final Logger logger = LoggerFactory.getLogger(NotificationUserActionObserver.class);

    private final MailService mailService;

    /**
     * Initialize and register with subject.
     *
     * @param subject The subject to observe
     * @param mailService The mail service for sending notifications
     */
    @Autowired
    public NotificationUserActionObserver(UserActionSubject subject, MailService mailService) {
        this.mailService = mailService;
        subject.addObserver(this);
    }

    @Override
    public void onUserAction(UserActionEvent event, User user, Object data) {
        switch (event) {
            case USER_REGISTERED:
                // Send welcome email to new users
                sendWelcomeEmail(user);
                break;
            case PASSWORD_CHANGED:
                // Notify user of password change for security
                sendPasswordChangedEmail(user);
                break;
            // Other notifications can be added as needed
            default:
                // No notification for other events
                break;
        }
    }

    /**
     * Send a welcome email to a new user.
     *
     * @param user The new user
     */
    private void sendWelcomeEmail(User user) {
        logger.debug("Sending welcome email to: {}", user.getEmail());
        try {
            String subject = "Bienvenue sur ExoExplorer";
            String content = "Bonjour " + (user.getFirstName() != null ? user.getFirstName() : "") + ",\n\n" +
                    "Bienvenue sur ExoExplorer, votre portail vers les étoiles !\n\n" +
                    "Explorez des exoplanètes fascinantes, créez votre liste de favoris et découvrez les merveilles de notre univers.\n\n" +
                    "L'équipe ExoExplorer";

            // To avoid sending real emails during development, comment out the line below
             mailService.sendEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", user.getEmail(), e);
        }
    }

    /**
     * Send a password changed email to a user.
     *
     * @param user The user
     */
    private void sendPasswordChangedEmail(User user) {
        logger.debug("Sending password changed email to: {}", user.getEmail());
        try {
            String subject = "Mot de passe modifié - ExoExplorer";
            String content = "Bonjour " + (user.getFirstName() != null ? user.getFirstName() : "") + ",\n\n" +
                    "Votre mot de passe a été modifié avec succès.\n\n" +
                    "Si vous n'êtes pas à l'origine de cette modification, veuillez contacter notre support immédiatement.\n\n" +
                    "L'équipe ExoExplorer";

            // Commented out to avoid actually sending emails during development
            // mailService.sendEmail(user.getEmail(), subject, content);
        } catch (Exception e) {
            logger.error("Failed to send password changed email to: {}", user.getEmail(), e);
        }
    }
}
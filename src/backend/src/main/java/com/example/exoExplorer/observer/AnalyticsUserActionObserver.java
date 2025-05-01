package com.example.exoExplorer.observer;

import com.example.exoExplorer.entities.Exoplanet;
import com.example.exoExplorer.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Analytics observer for user actions.
 * Collects analytics data for user actions.
 */
@Component
public class AnalyticsUserActionObserver implements UserActionObserver {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsUserActionObserver.class);

    /**
     * Initialize and register with subject.
     *
     * @param subject The subject to observe
     */
    public AnalyticsUserActionObserver(UserActionSubject subject) {
        subject.addObserver(this);
    }

    @Override
    public void onUserAction(UserActionEvent event, User user, Object data) {
        // In a real implementation, this would send analytics data to a service
        logger.debug("Analytics event: {} for user: {}", event, user.getEmail());

        // Example analytics collection based on event type
        switch (event) {
            case USER_REGISTERED:
                // Track user registration
                trackEvent("registration", user.getEmail());
                break;
            case USER_FAVORITE_ADDED:
                // Track favorite added with exoplanet info
                Exoplanet exo = (Exoplanet) data;
                trackEventWithProperties("favorite_added", user.getEmail(),
                        "exoplanet_id", exo.getId(),
                        "exoplanet_name", exo.getName());
                break;
            case USER_FAVORITE_REMOVED:
                // Track favorite removed
                Exoplanet removedExo = (Exoplanet) data;
                trackEventWithProperties("favorite_removed", user.getEmail(),
                        "exoplanet_id", removedExo.getId(),
                        "exoplanet_name", removedExo.getName());
                break;
            default:
                // Track other events
                trackEvent(event.toString().toLowerCase(), user.getEmail());
        }
    }

    /**
     * Track a simple event.
     */
    private void trackEvent(String eventName, String userId) {
        logger.debug("Tracking event: {} for user: {}", eventName, userId);
        // In a real implementation, this would send data to an analytics service
    }

    /**
     * Track an event with additional properties.
     */
    private void trackEventWithProperties(String eventName, String userId, Object... properties) {
        logger.debug("Tracking event: {} for user: {} with properties", eventName, userId);
        // In a real implementation, this would send data with properties to an analytics service
    }
}
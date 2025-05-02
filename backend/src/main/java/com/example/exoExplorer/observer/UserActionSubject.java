package com.example.exoExplorer.observer;

import com.example.exoExplorer.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Subject class for the Observer pattern.
 * Notifies observers when user actions occur.
 */
@Component
public class UserActionSubject {
    private static final Logger logger = LoggerFactory.getLogger(UserActionSubject.class);
    private final List<UserActionObserver> observers = new ArrayList<>();

    /**
     * Add an observer to the notification list.
     *
     * @param observer The observer to add
     */
    public void addObserver(UserActionObserver observer) {
        observers.add(observer);
    }

    /**
     * Remove an observer from the notification list.
     *
     * @param observer The observer to remove
     */
    public void removeObserver(UserActionObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notify all observers of a user action.
     *
     * @param event The type of event that occurred
     * @param user The user who performed the action
     * @param data Additional data related to the event
     */
    public void notifyObservers(UserActionEvent event, User user, Object data) {
        logger.debug("Notifying observers of event: {} for user: {}", event, user.getEmail());
        observers.forEach(observer -> observer.onUserAction(event, user, data));
    }
}
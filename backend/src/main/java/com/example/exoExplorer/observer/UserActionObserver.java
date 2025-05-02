package com.example.exoExplorer.observer;

import com.example.exoExplorer.entities.User;

/**
 * Observer interface for user actions.
 * Implements the Observer pattern.
 */
public interface UserActionObserver {

    /**
     * Called when a user action occurs.
     *
     * @param event The type of event that occurred
     * @param user The user who performed the action
     * @param data Additional data related to the event
     */
    void onUserAction(UserActionEvent event, User user, Object data);
}
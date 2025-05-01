package com.example.exoExplorer.exceptions;

/**
 * Exception thrown when trying to register a user that already exists.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
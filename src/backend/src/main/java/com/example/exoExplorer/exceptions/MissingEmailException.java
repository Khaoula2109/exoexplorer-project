package com.example.exoExplorer.exceptions;

/**
 * Exception thrown when an email is missing or blank.
 */
public class MissingEmailException extends RuntimeException {
    public MissingEmailException(String message) {
        super(message);
    }
}
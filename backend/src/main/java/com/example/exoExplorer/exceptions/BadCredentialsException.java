package com.example.exoExplorer.exceptions;

/**
 * Exception thrown when credentials are invalid.
 */
public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {
        super(message);
    }
}
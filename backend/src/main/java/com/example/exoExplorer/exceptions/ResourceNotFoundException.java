package com.example.exoExplorer.exceptions;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
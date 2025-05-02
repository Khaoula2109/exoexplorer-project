package com.example.exoExplorer.exceptions;

/**
 * Exception thrown when an OTP is invalid or expired.
 */
public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException(String message) {
        super(message);
    }
}
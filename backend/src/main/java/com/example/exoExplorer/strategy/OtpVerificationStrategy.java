package com.example.exoExplorer.strategy;

import com.example.exoExplorer.entities.User;
import com.example.exoExplorer.exceptions.InvalidOtpException;

/**
 * Interface for OTP verification strategies.
 * Implements the Strategy pattern.
 */
public interface OtpVerificationStrategy {
    /**
     * Verifies the OTP for the given user.
     *
     * @param user The user to verify OTP for
     * @param otp The OTP to verify
     * @throws InvalidOtpException If the OTP is invalid
     */
    void verify(User user, String otp) throws InvalidOtpException;
}
package com.example.exoExplorer.strategy;

import com.example.exoExplorer.entities.User;
import com.example.exoExplorer.exceptions.InvalidOtpException;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Standard OTP verification using BCrypt.
 */
@Component
public class StandardOtpVerificationStrategy implements OtpVerificationStrategy {

    @Override
    public void verify(User user, String otp) throws InvalidOtpException {
        if (user.getOtpCodeHash() == null || user.getOtpExpiry() == null) {
            throw new InvalidOtpException("Aucun OTP généré pour cet utilisateur");
        }

        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new InvalidOtpException("OTP expiré");
        }

        if (!BCrypt.checkpw(otp, user.getOtpCodeHash())) {
            throw new InvalidOtpException("OTP invalide");
        }
    }
}
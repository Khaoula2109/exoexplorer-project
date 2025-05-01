package com.example.exoExplorer.strategy;

import com.example.exoExplorer.entities.User;
import com.example.exoExplorer.exceptions.InvalidOtpException;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * Backup code verification strategy.
 */
@Component
public class BackupCodeVerificationStrategy implements OtpVerificationStrategy {

    @Override
    public void verify(User user, String backupCode) throws InvalidOtpException {
        // Implementation for backup code verification
        // This would check against stored backup codes and mark the used code
        boolean found = user.getBackupCodes().stream()
                .anyMatch(code -> !code.getUsed() && BCrypt.checkpw(backupCode, code.getBackupCode()));

        if (!found) {
            throw new InvalidOtpException("Code de secours invalide");
        }

        // Mark the backup code as used
        user.getBackupCodes().stream()
                .filter(code -> BCrypt.checkpw(backupCode, code.getBackupCode()))
                .findFirst()
                .ifPresent(code -> code.setUsed(true));
    }
}
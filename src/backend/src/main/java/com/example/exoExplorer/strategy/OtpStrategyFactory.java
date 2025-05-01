package com.example.exoExplorer.strategy;

import org.springframework.stereotype.Component;

/**
 * Factory for creating OTP verification strategies.
 */
@Component
public class OtpStrategyFactory {

    private final StandardOtpVerificationStrategy standardStrategy;
    private final BackupCodeVerificationStrategy backupStrategy;

    public OtpStrategyFactory(
            StandardOtpVerificationStrategy standardStrategy,
            BackupCodeVerificationStrategy backupStrategy) {
        this.standardStrategy = standardStrategy;
        this.backupStrategy = backupStrategy;
    }

    /**
     * Get the appropriate OTP verification strategy.
     *
     * @param isBackupCode True if using a backup code, false for standard OTP
     * @return The appropriate OTP verification strategy
     */
    public OtpVerificationStrategy getStrategy(boolean isBackupCode) {
        return isBackupCode ? backupStrategy : standardStrategy;
    }
}
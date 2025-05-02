package com.example.exoExplorer.config;

import com.example.exoExplorer.services.MailService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration for mail service.
 * Provides a mock mail service that captures OTPs for testing.
 */
@TestConfiguration
public class TestMailConfig {

    public static String lastOtp;

    @Bean
    public MailService mailService() {
        return new MailService() {
            @Override
            public void sendOtpEmail(String to, String otp) {
                System.out.println("📨 Mock email à " + to + " : OTP = " + otp);
                lastOtp = otp;
            }
        };
    }
}
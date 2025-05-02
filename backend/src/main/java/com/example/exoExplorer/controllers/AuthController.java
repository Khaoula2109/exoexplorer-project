package com.example.exoExplorer.controllers;

import com.example.exoExplorer.dto.LoginRequest;
import com.example.exoExplorer.dto.OtpVerificationRequest;
import com.example.exoExplorer.dto.SignupRequest;
import com.example.exoExplorer.entities.User;
import com.example.exoExplorer.services.TokenService;
import com.example.exoExplorer.services.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for authentication-related endpoints.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    /**
     * Registers a new user.
     *
     * @param request The signup request containing email and password
     * @return Response indicating success
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupRequest request) {
        logger.info("Signup request received for email: {}", request.getEmail());
        userService.registerUser(request.getEmail(), request.getPassword());
        userService.processLogin(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("message", "Inscription réussie"));
    }

    /**
     * Initiates login by sending an OTP.
     *
     * @param request The login request containing email and password
     * @return Response indicating success
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request received for email: {}", request.getEmail());
        userService.processLogin(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("message", "Code OTP envoyé par email"));
    }

    /**
     * Verifies an OTP to complete authentication.
     *
     * @param request The OTP verification request containing email and OTP
     * @return Response containing JWT token
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        logger.info("OTP verification request received for email: {}", request.getEmail());

        // Verifies the OTP and finalizes authentication
        User user = userService.verifyOtp(request.getEmail(), request.getOtp());

        // Generates a JWT token for the user
        String token = tokenService.generateToken(request.getEmail());

        // Returns a response containing the token
        return ResponseEntity.ok(Map.of(
                "message", "OTP vérifié avec succès. Connexion terminée.",
                "token", token,
                "isAdmin", user.isAdmin()
        ));
    }

    /**
     * Generates backup codes for a user.
     *
     * @param payload The request payload containing email and code count
     * @return List of generated backup codes
     */
    @PostMapping("/generate-backup-codes")
    public ResponseEntity<List<String>> generateBackupCodes(@RequestBody Map<String, Object> payload) {
        String email = (String) payload.get("email");
        int count = (Integer) payload.getOrDefault("count", 5);

        logger.info("Generating {} backup codes for email: {}", count, email);
        List<String> codes = userService.generateBackupCodes(email, count);
        return ResponseEntity.ok(codes);
    }

    /**
     * Verifies a backup code.
     *
     * @param payload The request payload containing email and backup code
     * @return Response indicating success
     */
    @PostMapping("/verify-backup-code")
    public ResponseEntity<Map<String, String>> verifyBackupCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String backupCode = payload.get("backupCode");

        logger.info("Verifying backup code for email: {}", email);
        boolean verified = userService.verifyBackupCode(email, backupCode);

        if (verified) {
            // Generate token if backup code is valid
            String token = tokenService.generateToken(email);
            return ResponseEntity.ok(Map.of(
                    "message", "Backup code vérifié avec succès",
                    "token", token
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Code de secours invalide"
            ));
        }
    }
}
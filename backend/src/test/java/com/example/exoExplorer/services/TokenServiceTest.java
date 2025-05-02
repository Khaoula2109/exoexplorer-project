package com.example.exoExplorer.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;
    private static final String TEST_SECRET_KEY = "TestSecretKeyWithAtLeast256BitsForHMACSHA256Security";
    private static final long TEST_EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setup() {
        tokenService = new TokenService();

        // Manually set the fields that would normally be injected by Spring
        ReflectionTestUtils.setField(tokenService, "secretKey", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(tokenService, "jwtExpiration", TEST_EXPIRATION);
    }

    @Test
    void testGenerateTokenNotNull() {
        String email = "test@example.com";
        String token = tokenService.generateToken(email);

        assertNotNull(token, "Le token ne doit pas être null");
        assertFalse(token.isEmpty(), "Le token ne doit pas être vide");
    }

    @Test
    void testExtractUsernameFromToken() {
        String email = "user@example.com";
        String token = tokenService.generateToken(email);

        String extracted = tokenService.extractUsername(token);

        assertEquals(email, extracted, "L'email extrait doit correspondre à l'email d'origine");
    }

    @Test
    void testTokenValidity() {
        // GIVEN
        String email = "test@example.com";
        UserDetails userDetails = new User(email, "password", new ArrayList<>());
        String token = tokenService.generateToken(email);

        // WHEN
        boolean isValid = tokenService.isTokenValid(token, userDetails);

        // THEN
        assertTrue(isValid, "Le token doit être valide pour l'utilisateur");
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        // For testing expiration, use a very short expiration time
        ReflectionTestUtils.setField(tokenService, "jwtExpiration", 1); // 1ms

        String email = "test@example.com";
        UserDetails userDetails = new User(email, "password", new ArrayList<>());
        String token = tokenService.generateToken(email);

        // Wait to ensure token expires
        Thread.sleep(10);

        // Test private method via reflection or test public methods that use it
        boolean isValid = tokenService.isTokenValid(token, userDetails);

        assertFalse(isValid, "Le token devrait être expiré");
    }
}
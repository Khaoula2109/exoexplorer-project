package com.example.exoExplorer.controllers;

import com.example.exoExplorer.config.TestSecurityConfig;
import com.example.exoExplorer.dto.*;
import com.example.exoExplorer.services.TokenService;
import com.example.exoExplorer.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired  private MockMvc       mockMvc;
    @Autowired  private ObjectMapper  objectMapper;

    @MockBean   private UserService   userService;
    @MockBean   private TokenService  tokenService;

    @MockBean private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @Test
    void testSignupSuccess() throws Exception {
        SignupRequest req = new SignupRequest("test@example.com", "password123");

        mockMvc.perform(post("/api/auth/signup").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequest req = new LoginRequest("test@example.com", "password123");

        mockMvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testVerifyOtpSuccess() throws Exception {
        OtpVerificationRequest req = new OtpVerificationRequest("test@example.com", "123456");
        String mockToken = "mock.jwt.token";
        Mockito.when(tokenService.generateToken("test@example.com")).thenReturn(mockToken);

        mockMvc.perform(post("/api/auth/verify-otp").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(mockToken))
                .andExpect(jsonPath("$.message").value("OTP vérifié avec succès. Connexion terminée."));

        verify(userService).verifyOtp("test@example.com", "123456");
    }

    @Test
    void testGenerateBackupCodes() throws Exception {
        Mockito.when(userService.generateBackupCodes("test@example.com", 3))
                .thenReturn(List.of("12345678", "87654321", "00001111"));

        mockMvc.perform(post("/api/auth/generate-backup-codes").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "test@example.com", "count", 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void testVerifyBackupCodeSuccess() throws Exception {
        Mockito.when(userService.verifyBackupCode("test@example.com", "12345678"))
                .thenReturn(true);

        String mockToken = "mock.jwt.token";
        Mockito.when(tokenService.generateToken("test@example.com")).thenReturn(mockToken);

        mockMvc.perform(post("/api/auth/verify-backup-code").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "test@example.com", "backupCode", "12345678"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(mockToken))
                .andExpect(jsonPath("$.message").exists());
    }
}
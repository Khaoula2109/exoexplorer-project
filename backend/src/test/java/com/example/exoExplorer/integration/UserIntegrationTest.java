package com.example.exoExplorer.integration;

import com.example.exoExplorer.entities.User;
import com.example.exoExplorer.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        userRepository.deleteAll(); // Cleaning

        User user = new User();
        user.setEmail("user@test.com");
        user.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt()));
        user.setOtpVerified(true);
        user.setFirstName("Test");
        user.setLastName("User");

        userRepository.save(user);
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void testGetProfile() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                        .param("email", "user@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").exists())
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void testUpdateProfile() throws Exception {
        mockMvc.perform(put("/api/user/update-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "user@test.com",
                                "firstName", "John",
                                "lastName", "Doe"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile updated successfully"));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void testChangePassword() throws Exception {
        // First set a known password
        User user = userRepository.findByEmail("user@test.com").orElseThrow();
        String initialPassword = "initial123";
        user.setPassword(BCrypt.hashpw(initialPassword, BCrypt.gensalt()));
        userRepository.save(user);

        // Now attempt to change it
        mockMvc.perform(post("/api/user/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "user@test.com",
                                "currentPassword", initialPassword,
                                "newPassword", "newPassword123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        // Verify password was changed
        User updatedUser = userRepository.findByEmail("user@test.com").orElseThrow();
        assert BCrypt.checkpw("newPassword123", updatedUser.getPassword());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void testUpdateProfile_missingEmail() throws Exception {
        mockMvc.perform(put("/api/user/update-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "John",
                                "lastName", "Doe"
                        ))))
                .andExpect(status().isBadRequest()); // Should fail with 400
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void testGetProfile_userNotFound() throws Exception {
        mockMvc.perform(get("/api/user/profile")
                        .param("email", "nonexistent@test.com"))
                .andExpect(status().isNotFound()); // Should fail with 404
    }
}
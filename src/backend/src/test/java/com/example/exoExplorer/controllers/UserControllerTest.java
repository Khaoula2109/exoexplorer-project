package com.example.exoExplorer.controllers;

import com.example.exoExplorer.config.TestSecurityConfig;
import com.example.exoExplorer.entities.Exoplanet;
import com.example.exoExplorer.entities.User;
import com.example.exoExplorer.services.UserService;
import com.example.exoExplorer.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetFavorites() throws Exception {
        Exoplanet exo = new Exoplanet();
        exo.setId(1);
        exo.setName("Kepler");

        Mockito.when(userService.getFavorites("test@example.com")).thenReturn(List.of(exo));

        mockMvc.perform(get("/api/user/favorites")
                        .param("email", "test@example.com")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Kepler"));
    }

    @Test
    void testToggleFavorite() throws Exception {
        Map<String, Object> body = Map.of("email", "test@example.com", "exoplanetId", 1);

        mockMvc.perform(post("/api/user/toggle-favorite")
                        .with(user("test@example.com").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Favorite toggled successfully"));
    }

    @Test
    void testGetProfile() throws Exception {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");

        Mockito.when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/user/profile")
                        .param("email", "test@example.com")
                        .with(user("test@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void testUpdateProfile() throws Exception {
        Map<String, String> body = Map.of(
                "email", "test@example.com",
                "firstName", "Jane",
                "lastName", "Doe"
        );

        mockMvc.perform(put("/api/user/update-profile")
                        .with(user("test@example.com").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile updated successfully"));
    }

    @Test
    void testChangePassword() throws Exception {
        Map<String, String> body = Map.of(
                "email", "test@example.com",
                "currentPassword", "old123",
                "newPassword", "new123"
        );

        mockMvc.perform(post("/api/user/change-password")
                        .with(user("test@example.com").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }
}
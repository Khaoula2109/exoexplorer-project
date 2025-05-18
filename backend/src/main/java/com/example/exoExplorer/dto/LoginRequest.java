package com.example.exoExplorer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for login requests.
 */
@Setter
@Getter
public class LoginRequest {

    // Getters and Setters
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    /**
     * Default constructor.
     * Required for JSON deserialization.
     */
    public LoginRequest() {}

    /**
     * Constructor with arguments.
     * Useful for testing.
     *
     * @param email User's email
     * @param password User's password
     */
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

}
package com.example.exoExplorer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for user registration requests.
 */
@Setter
@Getter
public class SignupRequest {

    // Getters and Setters
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit comporter au moins 6 caractères")
    private String password;

    /**
     * Default constructor.
     * Required for JSON deserialization.
     */
    public SignupRequest() {}

    /**
     * Constructor with arguments.
     * Useful for testing.
     *
     * @param email User's email
     * @param password User's password
     */
    public SignupRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

}
package com.example.exoExplorer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for OTP verification requests.
 */
@Setter
@Getter
public class OtpVerificationRequest {

    // Getters and Setters
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "L'OTP est obligatoire")
    @Size(min = 6, max = 6, message = "L'OTP doit comporter 6 chiffres")
    private String otp;

    /**
     * Default constructor.
     * Required for JSON deserialization.
     */
    public OtpVerificationRequest() {}

    /**
     * Constructor with arguments.
     * Useful for testing.
     *
     * @param email User's email
     * @param otp One-time password
     */
    public OtpVerificationRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }

}
package com.example.exoExplorer.exceptions;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler that provides consistent error responses
 * for different exception types across the application.
 * Implements a uniform error response format.
 */
@ControllerAdvice
public class GenericExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionHandler.class);

    /**
     * Creates a standardized error response format.
     * @param message Error message
     * @param status HTTP status code
     * @param ex The exception that occurred
     * @return Structured error response
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status, Exception ex) {
        logger.error("Exception handled: {} - {}", ex.getClass().getSimpleName(), message, ex);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidOtp(InvalidOtpException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(MissingEmailException.class)
    public ResponseEntity<Map<String, Object>> handleMissingEmail(MissingEmailException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String firstError = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("Erreur de validation");
        return createErrorResponse(firstError, HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        return createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return createErrorResponse("Paramètre '" + ex.getName() + "' invalide : " + ex.getValue(),
                HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return createErrorResponse("Corps JSON mal formé", HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<Map<String, Object>> handleMail(MailException ex) {
        return createErrorResponse("Impossible d'envoyer l'email pour l'instant",
                HttpStatus.SERVICE_UNAVAILABLE, ex);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> handleRestClient(RestClientException ex) {
        return createErrorResponse("Service exoplanets API indisponible", HttpStatus.BAD_GATEWAY, ex);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwt(JwtException ex) {
        return createErrorResponse("Token JWT invalide ou expiré", HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex) {
        return createErrorResponse("Violation d'intégrité des données", HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return createErrorResponse("Erreur interne du serveur", HttpStatus.INTERNAL_SERVER_ERROR, ex);
    }
}
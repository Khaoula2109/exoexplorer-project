package com.example.exoExplorer.e2e;

import com.example.exoExplorer.config.TestMailConfig;
import com.example.exoExplorer.config.TestSecurityConfig;
import com.example.exoExplorer.dto.LoginRequest;
import com.example.exoExplorer.dto.OtpVerificationRequest;
import com.example.exoExplorer.dto.SignupRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, TestMailConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SecurityIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + "/api" + path;
    }

    /* ==========================================================================
       1) Endpoints publics accessibles sans authentification
       ========================================================================== */
    @Test
    @Order(1)
    void public_exoplanetSummary_shouldSucceedWithoutAuth() {
        ResponseEntity<String> resp = restTemplate.getForEntity(url("/exoplanets/summary"), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(2)
    void public_signup_shouldSucceedWithoutAuth() {
        var req = new SignupRequest("security_test@test.com", "pass123");
        ResponseEntity<String> resp = restTemplate.postForEntity(url("/auth/signup"), req, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /* ==========================================================================
       2) Endpoints protégés inaccessibles sans JWT
       ========================================================================== */
    @Test
    @Order(3)
    void protected_profile_shouldFailWithoutJwt() {
        ResponseEntity<String> resp = restTemplate.getForEntity(url("/user/profile?email=test@user.com"), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(4)
    void protected_updateProfile_shouldFailWithInvalidJwt() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.jwt.token");
        headers.setContentType(MediaType.APPLICATION_JSON);

        var body = Map.of(
                "email", "test@user.com",
                "firstName", "Fake",
                "lastName", "User"
        );

        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/user/update-profile"), HttpMethod.PUT, entity, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(5)
    void updateProfile_withValidJwt_shouldSucceed() throws Exception {
        // Step 1: Signup (email déjà inscrit => ignorable si conflit)
        String testEmail = "secured@test.com";
        String password = "secure123";

        var signupReq = new SignupRequest(testEmail, password);
        restTemplate.postForEntity(url("/auth/signup"), signupReq, Void.class);

        // Step 2: Login to trigger OTP
        var loginReq = new LoginRequest(testEmail, password);
        restTemplate.postForEntity(url("/auth/login"), loginReq, Void.class);

        // Step 3: Récupère l'OTP simulé via TestMailConfig
        String otp = TestMailConfig.lastOtp;
        assertThat(otp).isNotBlank();

        // Step 4: Verify OTP
        var verifyReq = new OtpVerificationRequest(testEmail, otp);
        ResponseEntity<String> verifyResp = restTemplate.postForEntity(url("/auth/verify-otp"), verifyReq, String.class);

        assertThat(verifyResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        String jwt = new ObjectMapper().readTree(verifyResp.getBody()).get("token").asText();

        // Step 5: Appel sécurisé avec JWT
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "email", testEmail,
                "firstName", "Secure",
                "lastName", "Access"
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                url("/user/update-profile"), HttpMethod.PUT, entity, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
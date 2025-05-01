package com.example.exoExplorer.e2e;

import com.example.exoExplorer.config.TestMailConfig;
import com.example.exoExplorer.config.TestSecurityConfig;
import com.example.exoExplorer.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestMailConfig.class, TestSecurityConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String EMAIL = "test@user.com";
    private static final String PASSWORD = "pass1234";
    private static String jwtToken;            // partagé entre les tests
    private static Long firstExoplanetId;      // ID inséré par /test/reset-db

    private String url(String path) {
        return "http://localhost:" + port + "/api" + path;
    }

    @BeforeAll
    static void globalReset(@Autowired TestRestTemplate rt, @LocalServerPort int p) {
        String base = "http://localhost:" + p + "/api/test";
        rt.delete(base + "/reset-user?email=" + EMAIL);
        rt.delete(base + "/reset-db");
    }

    @Test @Order(1)
    void resetDb_and_seedExoplanet() {
        // 1-a) reset + seed (DELETE déclenche l'insertion)
        assertThat(restTemplate.exchange(
                        url("/test/reset-db"), HttpMethod.DELETE, null, Void.class)
                .getStatusCode()).isEqualTo(HttpStatus.OK);

        // 1-b) liste complète (endpoint renvoie un tableau JSON)
        ParameterizedTypeReference<List<Map<String,Object>>> listType = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<Map<String,Object>>> resp = restTemplate.exchange(url("/exoplanets"), HttpMethod.GET, null, listType);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();

        Map<String,Object> first = resp.getBody().get(0);

        // --- détection robuste de la clé « id » -----------------------------
        Object idRaw = first.getOrDefault("exoplanetId",
                first.getOrDefault("exoplanet_id", first.get("id")));

        assertThat(idRaw)
                .as("Impossible de trouver l'ID dans %s", first.keySet())
                .isInstanceOf(Number.class);

        firstExoplanetId = ((Number) idRaw).longValue();
        assertThat(firstExoplanetId).isPositive();
    }

    @Test @Order(2)
    void signup() {
        // On supprime toute trace éventuelle d'un ancien utilisateur
        restTemplate.delete(url("/test/reset-user?email=" + EMAIL));

        SignupRequest rq = new SignupRequest(EMAIL, PASSWORD);
        ResponseEntity<Void> resp = restTemplate.postForEntity(url("/auth/signup"), rq, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(3)
    void login_sendsOtp() {
        LoginRequest rq = new LoginRequest(EMAIL, PASSWORD);
        ResponseEntity<Void> resp = restTemplate.postForEntity(url("/auth/login"), rq, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(4)
    void verifyOtp_returnsJwt() throws Exception {
        String otp = TestMailConfig.lastOtp;
        assertThat(otp).isNotBlank();

        OtpVerificationRequest rq = new OtpVerificationRequest(EMAIL, otp);
        ResponseEntity<String> resp = restTemplate.postForEntity(url("/auth/verify-otp"), rq, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        jwtToken = new ObjectMapper()
                .readTree(resp.getBody())
                .get("token").asText();
        assertThat(jwtToken).isNotBlank();
    }

    @Test @Order(5)
    void profile_flow() {
        HttpHeaders hdrs = new HttpHeaders();
        hdrs.setBearerAuth(jwtToken);

        // 5-a) profil initial
        ResponseEntity<Map> profile = restTemplate.exchange(url("/user/profile?email=" + EMAIL),
                HttpMethod.GET, new HttpEntity<>(hdrs), Map.class);
        assertThat(profile.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 5-b) mise à jour
        hdrs.setContentType(MediaType.APPLICATION_JSON);
        Map<String,String> body = Map.of(
                "email", EMAIL,
                "firstName", "John",
                "lastName", "Doe");

        ResponseEntity<Void> upd = restTemplate.exchange(
                url("/user/update-profile"), HttpMethod.PUT,
                new HttpEntity<>(body, hdrs), Void.class);

        assertThat(upd.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test @Order(6)
    void favorites_flow() {
        assertThat(firstExoplanetId)
                .as("ID exoplanète introuvable — vérifie le seed")
                .isNotNull();

        HttpHeaders hdrs = new HttpHeaders();
        hdrs.setBearerAuth(jwtToken);
        hdrs.setContentType(MediaType.APPLICATION_JSON);

        /* ---------- 6-a) toggle ---------- */
        HttpEntity<Map<String,Object>> toggleRq = new HttpEntity<>(
                Map.of("email", EMAIL, "exoplanetId", firstExoplanetId), hdrs);

        assertThat(restTemplate.postForEntity(
                        url("/user/toggle-favorite"), toggleRq, Void.class)
                .getStatusCode()).isEqualTo(HttpStatus.OK);

        /* ---------- 6-b) liste ---------- */
        ParameterizedTypeReference<List<Map<String,Object>>> listType = new ParameterizedTypeReference<>() {};

        ResponseEntity<List<Map<String,Object>>> favs = restTemplate.exchange(
                url("/user/favorites?email=" + EMAIL),
                HttpMethod.GET,
                new HttpEntity<>(hdrs),
                listType);

        assertThat(favs.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(favs.getBody()).isNotEmpty();
    }
}
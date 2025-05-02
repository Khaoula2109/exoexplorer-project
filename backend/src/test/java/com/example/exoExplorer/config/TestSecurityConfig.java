package com.example.exoExplorer.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for test environment.
 * Allows access to test endpoints without authentication.
 */
@TestConfiguration
@Profile("test")
public class TestSecurityConfig {

    /**
     * High priority security chain for test endpoints.
     */
    @Bean
    @Order(0)
    public SecurityFilterChain testEndpoints(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/test/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
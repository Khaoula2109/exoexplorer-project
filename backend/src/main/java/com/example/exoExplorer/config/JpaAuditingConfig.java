package com.example.exoExplorer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Configuration class for JPA auditing.
 * Enables tracking of entity creation and modification.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    /**
     * Bean that provides the current auditor (user) for entity creation and modification.
     *
     * @return AuditorAware implementation that gets the current user from Spring Security
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of("system");
            }
            return Optional.of(auth.getName());
        };
    }
}
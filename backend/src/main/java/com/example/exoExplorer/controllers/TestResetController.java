package com.example.exoExplorer.controllers;

import com.example.exoExplorer.builder.ExoplanetBuilder;
import com.example.exoExplorer.entities.Exoplanet;
import com.example.exoExplorer.repositories.ExoplaneteRepository;
import com.example.exoExplorer.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for resetting test data.
 * Only available in the test profile.
 */
@RestController
@RequestMapping("/api/test")
@Profile("test")
public class TestResetController {
    private static final Logger logger = LoggerFactory.getLogger(TestResetController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExoplaneteRepository exoplanetRepository;

    /**
     * Resets a test user.
     *
     * @param email The email of the user to reset
     * @return Response indicating success
     */
    @DeleteMapping("/reset-user")
    public ResponseEntity<Void> resetUser(@RequestParam String email) {
        logger.info("Resetting test user: {}", email);
        userRepository.findByEmail(email).ifPresent(userRepository::delete);
        return ResponseEntity.ok().build();
    }

    /**
     * Resets the database and adds a test exoplanet.
     *
     * @return Response indicating success
     */
    @DeleteMapping("/reset-db")
    public ResponseEntity<Void> resetDbAndAddExoplanet() {
        logger.info("Resetting database and adding test exoplanet");
        exoplanetRepository.deleteAll();

        Exoplanet exo = new ExoplanetBuilder()
                .withName("Kepler-Test")
                .withDistance(42.0f)
                .withTemperature(273.0f)
                .withImage("https://example.com/kepler.png")
                .withRadius(1.0f)
                .withMass(1.0f)
                .withOrbitalPeriodDays(365.0f)
                .withOrbitalPeriodYear(1.0f)
                .build();

        exoplanetRepository.save(exo);

        return ResponseEntity.ok().build();
    }

    /**
     * Resets the database to a clean state.
     *
     * @return Response indicating success
     */
    @DeleteMapping("/reset-all")
    public ResponseEntity<Void> resetAll() {
        logger.info("Resetting entire database");
        exoplanetRepository.deleteAll();
        userRepository.deleteAll();
        return ResponseEntity.ok().build();
    }
}
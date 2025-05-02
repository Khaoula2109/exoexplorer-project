package com.example.exoExplorer.controllers;

import com.example.exoExplorer.builder.ExoplanetBuilder;
import com.example.exoExplorer.entities.Exoplanet;
import com.example.exoExplorer.repositories.ExoplaneteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Controller for data loading operations.
 * Admin-only endpoints for populating the database.
 */
@RestController
@RequestMapping("/api/admin/data-loader")
@PreAuthorize("hasAuthority('ADMIN')")
public class DataLoaderController {
    private static final Logger logger = LoggerFactory.getLogger(DataLoaderController.class);

    @Autowired
    private ExoplaneteRepository exoplanetRepository;

    private final Random random = new Random();

    /**
     * Inserts 500 test exoplanets into the database.
     *
     * @return Response indicating success
     */
    @PostMapping("/insert-500-exoplanets")
    public ResponseEntity<Map<String, String>> insertExoplanets() {
        logger.info("Inserting 500 test exoplanets");

        IntStream.rangeClosed(1, 500).forEach(i -> {
            Exoplanet exoplanet = new ExoplanetBuilder()
                    .withName("ExoTest-" + i)
                    .withDistance(random.nextFloat() * 5000)
                    .withTemperature(50 + random.nextFloat() * 450)
                    .withImage("https://picsum.photos/seed/exotest" + i + "/200")
                    .withYearDiscovered(1995 + random.nextInt(28))
                    .withRadius(0.5f + random.nextFloat() * 10)
                    .withMass(0.1f + random.nextFloat() * 20)
                    .withSemiMajorAxis(0.05f + random.nextFloat() * 50)
                    .withEccentricity(random.nextFloat() * 0.5f)
                    .withOrbitalPeriodDays(1 + random.nextFloat() * 1000)
                    .build();

            // Calculate orbital period in years
            if (exoplanet.getOrbitalPeriodDays() != null) {
                exoplanet.setOrbitalPeriodYear(exoplanet.getOrbitalPeriodDays() / 365.0f);
            }

            exoplanetRepository.save(exoplanet);
        });

        return ResponseEntity.ok(Map.of("message", "500 exoplanètes insérées avec succès."));
    }

    /**
     * Clears all exoplanet data from the database.
     *
     * @return Response indicating success
     */
    @DeleteMapping("/clear-exoplanets")
    public ResponseEntity<Map<String, String>> clearExoplanets() {
        logger.info("Clearing all exoplanet data");
        exoplanetRepository.deleteAll();
        return ResponseEntity.ok(Map.of("message", "Toutes les exoplanètes ont été supprimées."));
    }

    /**
     * Inserts sample habitable exoplanets.
     *
     * @return Response indicating success
     */
    @PostMapping("/insert-habitable-exoplanets")
    public ResponseEntity<Map<String, String>> insertHabitableExoplanets() {
        logger.info("Inserting sample habitable exoplanets");

        // Create several exoplanets with temperatures in the habitable range (180-310 K)
        String[] names = {
                "Kepler-186f", "Kepler-442b", "Kepler-62f", "Kepler-1649c",
                "TRAPPIST-1e", "TRAPPIST-1f", "Proxima Centauri b", "TOI-700d",
                "Teegarden's Star b", "K2-18b", "WASP-12b", "Wolf 1061c"
        };

        for (int i = 0; i < names.length; i++) {
            Exoplanet exoplanet = new ExoplanetBuilder()
                    .withName(names[i])
                    .withDistance(1 + random.nextFloat() * 200)
                    .withTemperature(180 + random.nextFloat() * 130) // Between 180K and 310K
                    .withImage("https://picsum.photos/seed/" + names[i].replace("'", "").replace(" ", "") + "/200")
                    .withYearDiscovered(2000 + random.nextInt(23))
                    .withRadius(0.5f + random.nextFloat() * 2) // Earth-like sizes
                    .withMass(0.5f + random.nextFloat() * 3)
                    .withSemiMajorAxis(0.5f + random.nextFloat() * 2)
                    .withEccentricity(random.nextFloat() * 0.2f)
                    .withOrbitalPeriodDays(100 + random.nextFloat() * 400)
                    .build();

            // Calculate orbital period in years
            if (exoplanet.getOrbitalPeriodDays() != null) {
                exoplanet.setOrbitalPeriodYear(exoplanet.getOrbitalPeriodDays() / 365.0f);
            }

            exoplanetRepository.save(exoplanet);
        }

        return ResponseEntity.ok(Map.of("message", names.length + " exoplanètes habitables insérées avec succès."));
    }
}
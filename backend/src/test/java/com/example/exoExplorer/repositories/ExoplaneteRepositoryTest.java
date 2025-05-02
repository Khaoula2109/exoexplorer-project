package com.example.exoExplorer.repositories;

import com.example.exoExplorer.entities.Exoplanet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class ExoplaneteRepositoryTest {

    @Autowired
    private ExoplaneteRepository exoplaneteRepository;

    @Test
    @DisplayName("Should find exoplanet by name (case-insensitive)")
    void testFindByNameIgnoreCase() {
        // Given
        Exoplanet exo = new Exoplanet();
        exo.setName("Kepler-22b");
        exo.setDistance(600.0f);
        exoplaneteRepository.save(exo);

        // When
        Optional<Exoplanet> found = exoplaneteRepository.findByNameIgnoreCase("kepler-22B");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Kepler-22b");
    }

    @Test
    @DisplayName("Should return empty when name not found")
    void testFindByNameIgnoreCase_NotFound() {
        // When
        Optional<Exoplanet> found = exoplaneteRepository.findByNameIgnoreCase("Unknown");

        // Then
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("Should find potentially habitable exoplanets")
    void testFindPotentiallyHabitable() {
        // Given
        Exoplanet habitableExo = new Exoplanet();
        habitableExo.setName("Habitable-1");
        habitableExo.setTemperature(280.0f); // Within habitable range
        exoplaneteRepository.save(habitableExo);

        Exoplanet coldExo = new Exoplanet();
        coldExo.setName("Cold-1");
        coldExo.setTemperature(150.0f); // Too cold
        exoplaneteRepository.save(coldExo);

        Exoplanet hotExo = new Exoplanet();
        hotExo.setName("Hot-1");
        hotExo.setTemperature(350.0f); // Too hot
        exoplaneteRepository.save(hotExo);

        // When
        List<Exoplanet> habitableExoplanets = exoplaneteRepository.findPotentiallyHabitable();

        // Then
        assertThat(habitableExoplanets).hasSize(1);
        assertThat(habitableExoplanets.get(0).getName()).isEqualTo("Habitable-1");
    }
}
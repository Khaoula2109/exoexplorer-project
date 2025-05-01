package com.example.exoExplorer.repositories;

import com.example.exoExplorer.entities.Exoplanet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Exoplanet entity.
 * Provides methods for accessing exoplanet data from the database.
 */
@Repository
public interface ExoplaneteRepository extends JpaRepository<Exoplanet, Integer>, JpaSpecificationExecutor<Exoplanet> {

    /**
     * Find an exoplanet by name (case insensitive).
     *
     * @param name The name to search for
     * @return An Optional containing the exoplanet if found
     */
    Optional<Exoplanet> findByNameIgnoreCase(String name);

    /**
     * Find exoplanets by temperature range.
     *
     * @param minTemp The minimum temperature
     * @param maxTemp The maximum temperature
     * @param pageable Pagination information
     * @return A Page of exoplanets within the temperature range
     */
    @Query("SELECT e FROM Exoplanet e WHERE e.temperature >= :minTemp AND e.temperature <= :maxTemp")
    Page<Exoplanet> findByTemperatureRange(@Param("minTemp") Float minTemp, @Param("maxTemp") Float maxTemp, Pageable pageable);

    /**
     * Find potentially habitable exoplanets (temperature between 180K and 310K).
     *
     * @return List of potentially habitable exoplanets
     */
    @Query("SELECT e FROM Exoplanet e WHERE e.temperature >= 180 AND e.temperature <= 310")
    List<Exoplanet> findPotentiallyHabitable();

    /**
     * Find exoplanets discovered in a specific year.
     *
     * @param year The year of discovery
     * @return List of exoplanets discovered in the given year
     */
    List<Exoplanet> findByYearDiscovered(Integer year);

    /**
     * Find exoplanets with radius similar to Earth (0.8 to 1.2 Earth radii).
     *
     * @return List of Earth-sized exoplanets
     */
    @Query("SELECT e FROM Exoplanet e WHERE e.radius >= 0.8 AND e.radius <= 1.2")
    List<Exoplanet> findEarthSized();

    /**
     * Count exoplanets by temperature range.
     *
     * @param minTemp The minimum temperature
     * @param maxTemp The maximum temperature
     * @return Number of exoplanets in the temperature range
     */
    @Query("SELECT COUNT(e) FROM Exoplanet e WHERE e.temperature >= :minTemp AND e.temperature <= :maxTemp")
    Long countByTemperatureRange(@Param("minTemp") Float minTemp, @Param("maxTemp") Float maxTemp);
}
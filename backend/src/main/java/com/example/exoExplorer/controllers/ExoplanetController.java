package com.example.exoExplorer.controllers;

import com.example.exoExplorer.decorator.ExoplanetDecoratorFactory;
import com.example.exoExplorer.dto.ExoplanetSummaryDTO;
import com.example.exoExplorer.entities.Exoplanet;
import com.example.exoExplorer.services.ExoplanetService;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controller for exoplanet-related endpoints.
 */
@RestController
@RequestMapping("/api/exoplanets")
public class ExoplanetController {
    private static final Logger logger = LoggerFactory.getLogger(ExoplanetController.class);

    @Autowired
    private ExoplanetService exoplanetService;

    /**
     * Refreshes exoplanet data from external source.
     *
     * @return Response indicating success
     */
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> refreshData() {
        logger.info("Refresh exoplanet data requested");
        exoplanetService.refreshExoplanetData();
        return ResponseEntity.ok("Exoplanet data refreshed successfully");
    }

    /**
     * Gets a page of exoplanet summaries with optional filtering.
     *
     * @param name Optional name filter
     * @param minTemp Optional minimum temperature filter
     * @param maxTemp Optional maximum temperature filter
     * @param minDistance Optional minimum distance filter
     * @param maxDistance Optional maximum distance filter
     * @param minYear Optional minimum year discovered filter
     * @param maxYear Optional maximum year discovered filter
     * @param pageable Pagination information
     * @return A page of exoplanet summaries
     */
    @GetMapping("/summary")
    public ResponseEntity<Page<ExoplanetSummaryDTO>> getExoplanetSummaries(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Float minTemp,
            @RequestParam(required = false) Float maxTemp,
            @RequestParam(required = false) Float minDistance,
            @RequestParam(required = false) Float maxDistance,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Specification<Exoplanet> spec = (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (name != null && !name.isEmpty()) {
                predicate = cb.and(predicate,
                        cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (minTemp != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("temperature"), minTemp));
            }

            if (maxTemp != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("temperature"), maxTemp));
            }

            if (minDistance != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("distance"), minDistance));
            }

            if (maxDistance != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("distance"), maxDistance));
            }

            if (minYear != null) {
                predicate = cb.and(predicate,
                        cb.greaterThanOrEqualTo(root.get("yearDiscovered"), minYear));
            }

            if (maxYear != null) {
                predicate = cb.and(predicate,
                        cb.lessThanOrEqualTo(root.get("yearDiscovered"), maxYear));
            }

            return predicate;
        };

        Page<ExoplanetSummaryDTO> page = exoplanetService.getExoplanetSummaries(spec, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Gets a list of all exoplanets.
     *
     * @return List of all exoplanets
     */
    @GetMapping
    public ResponseEntity<List<Exoplanet>> getAllExoplanets() {
        List<Exoplanet> exoplanets = exoplanetService.getAllExoplanets();
        return ResponseEntity.ok(exoplanets);
    }

    /**
     * Gets an exoplanet by ID.
     *
     * @param id The exoplanet ID
     * @return The exoplanet with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Exoplanet> getExoplanetById(@PathVariable Integer id) {
        Exoplanet exoplanet = exoplanetService.getExoplanetById(id);
        return ResponseEntity.ok(exoplanet);
    }

    /**
     * Gets an exoplanet by ID with enhanced details.
     *
     * @param id The exoplanet ID
     * @return Enhanced exoplanet data
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<Object> getExoplanetWithDetails(@PathVariable Integer id) {
        Object exoplanet = exoplanetService.getExoplanetWithDetails(id);
        return ResponseEntity.ok(exoplanet);
    }

    /**
     * Creates a new exoplanet.
     *
     * @param exoplanet The exoplanet to create
     * @return The created exoplanet
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Exoplanet> createExoplanet(@Valid @RequestBody Exoplanet exoplanet) {
        Exoplanet createdExoplanet = exoplanetService.createExoplanet(exoplanet);
        return new ResponseEntity<>(createdExoplanet, HttpStatus.CREATED);
    }

    /**
     * Updates an existing exoplanet.
     *
     * @param id The exoplanet ID
     * @param exoplanetDetails The updated exoplanet details
     * @return The updated exoplanet
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Exoplanet> updateExoplanet(
            @PathVariable Integer id,
            @Valid @RequestBody Exoplanet exoplanetDetails) {
        Exoplanet updatedExoplanet = exoplanetService.updateExoplanet(id, exoplanetDetails);
        return ResponseEntity.ok(updatedExoplanet);
    }

    /**
     * Deletes an exoplanet.
     *
     * @param id The exoplanet ID
     * @return Response indicating success
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteExoplanet(@PathVariable Integer id) {
        exoplanetService.deleteExoplanet(id);
        return ResponseEntity.ok(Map.of("message", "Exoplanet deleted successfully"));
    }

    /**
     * Gets potentially habitable exoplanets.
     *
     * @return List of potentially habitable exoplanets
     */
    @GetMapping("/habitable")
    public ResponseEntity<List<Object>> getHabitableExoplanets() {
        List<Exoplanet> exoplanets = exoplanetService.getAllExoplanets();

        List<Object> habitableExoplanets = Collections.singletonList(exoplanets.stream()
                .filter(exo -> exo.getTemperature() != null &&
                        exo.getTemperature() >= 180 &&
                        exo.getTemperature() <= 310)
                .map(exo -> ExoplanetDecoratorFactory.createWithHabitability(exo))
                .toList());

        return ResponseEntity.ok(habitableExoplanets);
    }
}
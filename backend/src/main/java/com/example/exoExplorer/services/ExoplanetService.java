package com.example.exoExplorer.services;

import com.example.exoExplorer.builder.ExoplanetBuilder;
import com.example.exoExplorer.decorator.ExoplanetDecoratorFactory;
import com.example.exoExplorer.dto.ExoplanetSummaryDTO;
import com.example.exoExplorer.entities.Exoplanet;
import com.example.exoExplorer.exceptions.ResourceNotFoundException;
import com.example.exoExplorer.repositories.ExoplaneteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing exoplanet data.
 * Incorporates Builder, Factory, and Decorator patterns.
 */
@Service
public class ExoplanetService {
    private static final Logger logger = LoggerFactory.getLogger(ExoplanetService.class);

    @Autowired
    private ExoplaneteRepository exoplanetRepository;

    @Autowired
    private ExternalExoplanetClient externalClient;

    @Autowired
    private ExoplanetImageService imageService;

    @Value("${exoplanet.travel-speed-fraction:0.1}")
    private float defaultTravelSpeedFraction;

    /**
     * Refreshes exoplanet data from external source.
     */
    @Transactional
    @CacheEvict(value = {"exoplanetSummaries", "exoplanetDetails"}, allEntries = true)
    public void refreshExoplanetData() {
        logger.info("Starting exoplanet data refresh");
        List<ExternalExoplanetClient.ExoplanetDTO> externalData = externalClient.fetchExoplanetData();
        int updated = 0;
        int created = 0;

        for (ExternalExoplanetClient.ExoplanetDTO dto : externalData) {
            try {
                Optional<Exoplanet> existingOpt = exoplanetRepository.findByNameIgnoreCase(dto.getPlName());

                if (existingOpt.isPresent()) {
                    // Update existing exoplanet using builder pattern
                    Exoplanet updated_exo = new ExoplanetBuilder()
                            .withExoplanet(existingOpt.get())
                            .withRadius(dto.getAvgRade() != null ? dto.getAvgRade().floatValue() : null)
                            .withMass(dto.getAvgMass() != null ? dto.getAvgMass().floatValue() : null)
                            .withDistance(dto.getAvgDist() != null ? dto.getAvgDist().floatValue() : null)
                            .withOrbitalPeriodDays(dto.getAvgPeriod() != null ? dto.getAvgPeriod().floatValue() : null)
                            .withOrbitalPeriodYear(dto.getAvgPeriod() != null ? dto.getAvgPeriod().floatValue() / 365.0f : null)
                            .withTemperature(dto.getAvgTemp() != null ? dto.getAvgTemp().floatValue() : null)
                            .build();

                    exoplanetRepository.save(updated_exo);
                    updated++;
                } else {
                    // Create new exoplanet using builder pattern
                    String imageUrl = imageService.getImageUrl(dto.getPlName());

                    Exoplanet newExo = new ExoplanetBuilder()
                            .withName(dto.getPlName())
                            .withRadius(dto.getAvgRade() != null ? dto.getAvgRade().floatValue() : null)
                            .withMass(dto.getAvgMass() != null ? dto.getAvgMass().floatValue() : null)
                            .withDistance(dto.getAvgDist() != null ? dto.getAvgDist().floatValue() : null)
                            .withOrbitalPeriodDays(dto.getAvgPeriod() != null ? dto.getAvgPeriod().floatValue() : null)
                            .withOrbitalPeriodYear(dto.getAvgPeriod() != null ? dto.getAvgPeriod().floatValue() / 365.0f : null)
                            .withTemperature(dto.getAvgTemp() != null ? dto.getAvgTemp().floatValue() : null)
                            .withImage(imageUrl)
                            .build();

                    exoplanetRepository.save(newExo);
                    created++;
                }
            } catch (DataIntegrityViolationException e) {
                logger.error("Data integrity violation when processing exoplanet: " + dto.getPlName(), e);} catch (Exception e) {
                logger.error("Error processing exoplanet: " + dto.getPlName(), e);
            }
        }

        logger.info("Exoplanet data refresh completed: {} updated, {} created", updated, created);
    }

    /**
     * Gets a page of exoplanet summaries.
     *
     * @param spec The specification for filtering
     * @param pageable The pagination information
     * @return A page of exoplanet summaries
     */
    @Cacheable(value = "exoplanetSummaries", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #spec.toString()")
    public Page<ExoplanetSummaryDTO> getExoplanetSummaries(Specification<Exoplanet> spec, Pageable pageable) {
        return exoplanetRepository.findAll(spec, pageable)
                .map(exo -> {
                    ExoplanetSummaryDTO dto = new ExoplanetSummaryDTO(exo.getId(), exo.getName(), exo.getImageExo());
                    return dto;
                });
    }

    /**
     * Gets an exoplanet by ID with enhanced details.
     *
     * @param id The exoplanet ID
     * @return Enhanced exoplanet details
     * @throws ResourceNotFoundException If the exoplanet is not found
     */
    @Cacheable(value = "exoplanetDetails", key = "#id")
    public Object getExoplanetWithDetails(Integer id) {
        Exoplanet exoplanet = exoplanetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exoplanète introuvable: " + id));

        // Use decorator pattern to enhance exoplanet with additional information
        return ExoplanetDecoratorFactory.createFullyFeatured(exoplanet, defaultTravelSpeedFraction);
    }

    /**
     * Gets all exoplanets.
     *
     * @return List of all exoplanets
     */
    public List<Exoplanet> getAllExoplanets() {
        return exoplanetRepository.findAll();
    }

    /**
     * Gets an exoplanet by ID.
     *
     * @param id The exoplanet ID
     * @return The exoplanet if found
     * @throws ResourceNotFoundException If the exoplanet is not found
     */
    public Exoplanet getExoplanetById(Integer id) {
        return exoplanetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exoplanète introuvable: " + id));
    }

    /**
     * Creates a new exoplanet.
     *
     * @param exoplanet The exoplanet to create
     * @return The created exoplanet
     */
    @Transactional
    @CacheEvict(value = {"exoplanetSummaries", "exoplanetDetails"}, allEntries = true)
    public Exoplanet createExoplanet(Exoplanet exoplanet) {
        logger.info("Creating new exoplanet: {}", exoplanet.getName());
        return exoplanetRepository.save(exoplanet);
    }

    /**
     * Updates an existing exoplanet.
     *
     * @param id The exoplanet ID
     * @param exoplanetDetails The updated exoplanet details
     * @return The updated exoplanet
     * @throws ResourceNotFoundException If the exoplanet is not found
     */
    @Transactional
    @CacheEvict(value = {"exoplanetSummaries", "exoplanetDetails"}, key = "#id")
    public Exoplanet updateExoplanet(Integer id, Exoplanet exoplanetDetails) {
        Exoplanet existingExoplanet = exoplanetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exoplanète introuvable: " + id));

        // Update using Builder pattern
        Exoplanet updated = new ExoplanetBuilder()
                .withExoplanet(existingExoplanet)
                .withName(exoplanetDetails.getName())
                .withImage(exoplanetDetails.getImageExo())
                .withDistance(exoplanetDetails.getDistance())
                .withTemperature(exoplanetDetails.getTemperature())
                .withYearDiscovered(exoplanetDetails.getYearDiscovered())
                .withRadius(exoplanetDetails.getRadius())
                .withMass(exoplanetDetails.getMasse())
                .withSemiMajorAxis(exoplanetDetails.getSemiMajorAxis())
                .withEccentricity(exoplanetDetails.getEccentricity())
                .withOrbitalPeriodYear(exoplanetDetails.getOrbitalPeriodYear())
                .withOrbitalPeriodDays(exoplanetDetails.getOrbitalPeriodDays())
                .build();

        logger.info("Updating exoplanet: {}", updated.getName());
        return exoplanetRepository.save(updated);
    }

    /**
     * Deletes an exoplanet.
     *
     * @param id The exoplanet ID
     * @throws ResourceNotFoundException If the exoplanet is not found
     */
    @Transactional
    @CacheEvict(value = {"exoplanetSummaries", "exoplanetDetails"}, allEntries = true)
    public void deleteExoplanet(Integer id) {
        Exoplanet exoplanet = exoplanetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exoplanète introuvable: " + id));

        logger.info("Deleting exoplanet: {}", exoplanet.getName());
        exoplanetRepository.delete(exoplanet);
    }

    /**
     * Gets potentially habitable exoplanets.
     *
     * @return List of potentially habitable exoplanets
     */
    public List<Exoplanet> getPotentiallyHabitableExoplanets() {
        return exoplanetRepository.findPotentiallyHabitable();
    }

    /**
     * Gets exoplanets with Earth-like characteristics.
     *
     * @return List of Earth-like exoplanets
     */
    public List<Exoplanet> getEarthLikeExoplanets() {
        return exoplanetRepository.findEarthSized();
    }
}
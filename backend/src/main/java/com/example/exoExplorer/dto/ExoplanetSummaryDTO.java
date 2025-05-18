package com.example.exoExplorer.dto;

import lombok.Getter;

/**
 * Data Transfer Object for summarized exoplanet information.
 * Used for list views and search results.
 */
@Getter
public class ExoplanetSummaryDTO {
    // Getters
    private Integer id;
    private String name;
    private String imageExo;

    /**
     * Constructor for creating an ExoplanetSummaryDTO.
     *
     * @param id The exoplanet ID
     * @param name The exoplanet name
     * @param imageExo The exoplanet image URL
     */
    public ExoplanetSummaryDTO(Integer id, String name, String imageExo) {
        this.id = id;
        this.name = name;
        this.imageExo = imageExo;
    }

}
package com.example.exoExplorer.dto;

/**
 * Data Transfer Object for summarized exoplanet information.
 * Used for list views and search results.
 */
public class ExoplanetSummaryDTO {
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

    // Getters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getImageExo() { return imageExo; }
}
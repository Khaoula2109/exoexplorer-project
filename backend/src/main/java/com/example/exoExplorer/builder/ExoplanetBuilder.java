package com.example.exoExplorer.builder;

import com.example.exoExplorer.entities.Exoplanet;

/**
 * Builder pattern implementation for Exoplanet entity.
 * Allows for more expressive and flexible creation of Exoplanet objects.
 */
public class ExoplanetBuilder {
    private final Exoplanet exoplanet;

    /**
     * Constructs a new ExoplanetBuilder with a new Exoplanet instance.
     */
    public ExoplanetBuilder() {
        this.exoplanet = new Exoplanet();
    }

    /**
     * Sets the ID of the exoplanet.
     *
     * @param id The ID to set
     * @return This builder
     */
    public ExoplanetBuilder withId(Integer id) {
        exoplanet.setId(id);
        return this;
    }

    /**
     * Sets the name of the exoplanet.
     *
     * @param name The name to set
     * @return This builder
     */
    public ExoplanetBuilder withName(String name) {
        exoplanet.setName(name);
        return this;
    }

    /**
     * Sets the image URL of the exoplanet.
     *
     * @param imageUrl The image URL to set
     * @return This builder
     */
    public ExoplanetBuilder withImage(String imageUrl) {
        exoplanet.setImageExo(imageUrl);
        return this;
    }

    /**
     * Sets the distance of the exoplanet.
     *
     * @param distance The distance to set
     * @return This builder
     */
    public ExoplanetBuilder withDistance(Float distance) {
        exoplanet.setDistance(distance);
        return this;
    }

    /**
     * Sets the temperature of the exoplanet.
     *
     * @param temperature The temperature to set
     * @return This builder
     */
    public ExoplanetBuilder withTemperature(Float temperature) {
        exoplanet.setTemperature(temperature);
        return this;
    }

    /**
     * Sets the year discovered of the exoplanet.
     *
     * @param yearDiscovered The year discovered to set
     * @return This builder
     */
    public ExoplanetBuilder withYearDiscovered(Integer yearDiscovered) {
        exoplanet.setYearDiscovered(yearDiscovered);
        return this;
    }

    /**
     * Sets the radius of the exoplanet.
     *
     * @param radius The radius to set
     * @return This builder
     */
    public ExoplanetBuilder withRadius(Float radius) {
        exoplanet.setRadius(radius);
        return this;
    }

    /**
     * Sets the mass of the exoplanet.
     *
     * @param mass The mass to set
     * @return This builder
     */
    public ExoplanetBuilder withMass(Float mass) {
        exoplanet.setMasse(mass);
        return this;
    }

    /**
     * Sets the semi-major axis of the exoplanet.
     *
     * @param semiMajorAxis The semi-major axis to set
     * @return This builder
     */
    public ExoplanetBuilder withSemiMajorAxis(Float semiMajorAxis) {
        exoplanet.setSemiMajorAxis(semiMajorAxis);
        return this;
    }

    /**
     * Sets the eccentricity of the exoplanet.
     *
     * @param eccentricity The eccentricity to set
     * @return This builder
     */
    public ExoplanetBuilder withEccentricity(Float eccentricity) {
        exoplanet.setEccentricity(eccentricity);
        return this;
    }

    /**
     * Sets the orbital period in years of the exoplanet.
     *
     * @param orbitalPeriodYear The orbital period in years to set
     * @return This builder
     */
    public ExoplanetBuilder withOrbitalPeriodYear(Float orbitalPeriodYear) {
        exoplanet.setOrbitalPeriodYear(orbitalPeriodYear);
        return this;
    }

    /**
     * Sets the orbital period in days of the exoplanet.
     *
     * @param orbitalPeriodDays The orbital period in days to set
     * @return This builder
     */
    public ExoplanetBuilder withOrbitalPeriodDays(Float orbitalPeriodDays) {
        exoplanet.setOrbitalPeriodDays(orbitalPeriodDays);
        return this;
    }

    /**
     * Creates a copy of the given exoplanet.
     *
     * @param existingExoplanet The exoplanet to copy
     * @return This builder with all properties set to match the existing exoplanet
     */
    public ExoplanetBuilder withExoplanet(Exoplanet existingExoplanet) {
        this.exoplanet.setId(existingExoplanet.getId());
        this.exoplanet.setName(existingExoplanet.getName());
        this.exoplanet.setImageExo(existingExoplanet.getImageExo());
        this.exoplanet.setDistance(existingExoplanet.getDistance());
        this.exoplanet.setTemperature(existingExoplanet.getTemperature());
        this.exoplanet.setYearDiscovered(existingExoplanet.getYearDiscovered());
        this.exoplanet.setRadius(existingExoplanet.getRadius());
        this.exoplanet.setMasse(existingExoplanet.getMasse());
        this.exoplanet.setSemiMajorAxis(existingExoplanet.getSemiMajorAxis());
        this.exoplanet.setEccentricity(existingExoplanet.getEccentricity());
        this.exoplanet.setOrbitalPeriodYear(existingExoplanet.getOrbitalPeriodYear());
        this.exoplanet.setOrbitalPeriodDays(existingExoplanet.getOrbitalPeriodDays());
        return this;
    }

    /**
     * Builds the Exoplanet instance.
     *
     * @return The constructed Exoplanet
     */
    public Exoplanet build() {
        return exoplanet;
    }
}
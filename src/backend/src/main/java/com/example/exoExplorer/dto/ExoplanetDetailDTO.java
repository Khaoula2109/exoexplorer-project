package com.example.exoExplorer.dto;

/**
 * Data Transfer Object for detailed exoplanet information.
 * Used for individual exoplanet views.
 */
public class ExoplanetDetailDTO {
    private Integer id;
    private String name;
    private String imageExo;
    private Float distance;
    private Float temperature;
    private Integer yearDiscovered;
    private Float radius;
    private Float mass;
    private Float semiMajorAxis;
    private Float eccentricity;
    private Float orbitalPeriodYear;
    private Float orbitalPeriodDays;
    private boolean potentiallyHabitable;
    private String earthSizeComparison;
    private String earthMassComparison;
    private Float travelTimeYears;

    /**
     * Default constructor.
     */
    public ExoplanetDetailDTO() {}

    /**
     * Builder pattern for creating ExoplanetDetailDTO.
     */
    public static class Builder {
        private ExoplanetDetailDTO dto = new ExoplanetDetailDTO();

        public Builder withId(Integer id) {
            dto.id = id;
            return this;
        }

        public Builder withName(String name) {
            dto.name = name;
            return this;
        }

        public Builder withImageExo(String imageExo) {
            dto.imageExo = imageExo;
            return this;
        }

        public Builder withDistance(Float distance) {
            dto.distance = distance;
            return this;
        }

        public Builder withTemperature(Float temperature) {
            dto.temperature = temperature;
            return this;
        }

        public Builder withYearDiscovered(Integer yearDiscovered) {
            dto.yearDiscovered = yearDiscovered;
            return this;
        }

        public Builder withRadius(Float radius) {
            dto.radius = radius;
            return this;
        }

        public Builder withMass(Float mass) {
            dto.mass = mass;
            return this;
        }

        public Builder withSemiMajorAxis(Float semiMajorAxis) {
            dto.semiMajorAxis = semiMajorAxis;
            return this;
        }

        public Builder withEccentricity(Float eccentricity) {
            dto.eccentricity = eccentricity;
            return this;
        }

        public Builder withOrbitalPeriodYear(Float orbitalPeriodYear) {
            dto.orbitalPeriodYear = orbitalPeriodYear;
            return this;
        }

        public Builder withOrbitalPeriodDays(Float orbitalPeriodDays) {
            dto.orbitalPeriodDays = orbitalPeriodDays;
            return this;
        }

        public Builder isPotentiallyHabitable(boolean potentiallyHabitable) {
            dto.potentiallyHabitable = potentiallyHabitable;
            return this;
        }

        public Builder withEarthSizeComparison(String earthSizeComparison) {
            dto.earthSizeComparison = earthSizeComparison;
            return this;
        }

        public Builder withEarthMassComparison(String earthMassComparison) {
            dto.earthMassComparison = earthMassComparison;
            return this;
        }

        public Builder withTravelTimeYears(Float travelTimeYears) {
            dto.travelTimeYears = travelTimeYears;
            return this;
        }

        public ExoplanetDetailDTO build() {
            return dto;
        }
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageExo() { return imageExo; }
    public void setImageExo(String imageExo) { this.imageExo = imageExo; }

    public Float getDistance() { return distance; }
    public void setDistance(Float distance) { this.distance = distance; }

    public Float getTemperature() { return temperature; }
    public void setTemperature(Float temperature) { this.temperature = temperature; }

    public Integer getYearDiscovered() { return yearDiscovered; }
    public void setYearDiscovered(Integer yearDiscovered) { this.yearDiscovered = yearDiscovered; }

    public Float getRadius() { return radius; }
    public void setRadius(Float radius) { this.radius = radius; }

    public Float getMass() { return mass; }
    public void setMass(Float mass) { this.mass = mass; }

    public Float getSemiMajorAxis() { return semiMajorAxis; }
    public void setSemiMajorAxis(Float semiMajorAxis) { this.semiMajorAxis = semiMajorAxis; }

    public Float getEccentricity() { return eccentricity; }
    public void setEccentricity(Float eccentricity) { this.eccentricity = eccentricity; }

    public Float getOrbitalPeriodYear() { return orbitalPeriodYear; }
    public void setOrbitalPeriodYear(Float orbitalPeriodYear) { this.orbitalPeriodYear = orbitalPeriodYear; }

    public Float getOrbitalPeriodDays() { return orbitalPeriodDays; }
    public void setOrbitalPeriodDays(Float orbitalPeriodDays) { this.orbitalPeriodDays = orbitalPeriodDays; }

    public boolean isPotentiallyHabitable() { return potentiallyHabitable; }
    public void setPotentiallyHabitable(boolean potentiallyHabitable) { this.potentiallyHabitable = potentiallyHabitable; }

    public String getEarthSizeComparison() { return earthSizeComparison; }
    public void setEarthSizeComparison(String earthSizeComparison) { this.earthSizeComparison = earthSizeComparison; }

    public String getEarthMassComparison() { return earthMassComparison; }
    public void setEarthMassComparison(String earthMassComparison) { this.earthMassComparison = earthMassComparison; }

    public Float getTravelTimeYears() { return travelTimeYears; }
    public void setTravelTimeYears(Float travelTimeYears) { this.travelTimeYears = travelTimeYears; }
}
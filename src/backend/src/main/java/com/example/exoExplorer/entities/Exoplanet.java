package com.example.exoExplorer.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Exoplanet entity class representing exoplanets in the application.
 * Extends BaseEntity to inherit auditing functionality.
 */
@Entity
@Table(name = "Exoplanet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"favoredBy"}, callSuper = true)
public class Exoplanet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exoplanet_id")
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "imageExo")
    private String imageExo;

    @Column(name = "distance")
    private Float distance;

    @Column(name = "temperature")
    private Float temperature;

    @Column(name = "year_discovered")
    private Integer yearDiscovered;

    @Column(name = "radius")
    private Float radius;

    @Column(name = "masse")
    private Float masse;

    @Column(name = "semi_major_axis")
    private Float semiMajorAxis;

    @Column(name = "eccentricity")
    private Float eccentricity;

    @Column(name = "orbital_period_year")
    private Float orbitalPeriodYear;

    @Column(name = "orbital_period_days")
    private Float orbitalPeriodDays;

    @ManyToMany(mappedBy = "favorites")
    @JsonIgnore
    private Set<User> favoredBy = new HashSet<>();

    /**
     * Calculate the estimated travel time in years at a given speed (light years per year).
     *
     * @param speedInLightYears Speed as a fraction of light speed (1.0 = speed of light)
     * @return Travel time in years
     */
    @Transient
    public Float calculateTravelTime(Float speedInLightYears) {
        if (distance == null || speedInLightYears == null || speedInLightYears <= 0) {
            return null;
        }
        return distance / speedInLightYears;
    }

    /**
     * Determine if the exoplanet is potentially habitable based on temperature.
     *
     * @return true if the temperature is within habitable range
     */
    @Transient
    public boolean isPotentiallyHabitable() {
        return temperature != null && temperature >= 180 && temperature <= 310;
    }

    /**
     * Get the orbital period in the most appropriate unit.
     *
     * @return A string representing the orbital period with appropriate unit
     */
    @Transient
    public String getFormattedOrbitalPeriod() {
        if (orbitalPeriodDays == null) {
            return "Unknown";
        }

        if (orbitalPeriodDays < 100) {
            return orbitalPeriodDays + " days";
        } else {
            return (orbitalPeriodDays / 365) + " years";
        }
    }
}
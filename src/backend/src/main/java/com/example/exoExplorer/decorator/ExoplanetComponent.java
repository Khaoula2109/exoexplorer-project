package com.example.exoExplorer.decorator;

/**
 * Base component interface for the Exoplanet decorator pattern.
 */
public interface ExoplanetComponent {
    String getName();
    Float getDistance();
    Float getTemperature();
    Integer getYearDiscovered();
    Float getRadius();
    Float getMass();
    Float getOrbitalPeriod();
    String getDescription();
}
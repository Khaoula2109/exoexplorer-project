package com.example.exoExplorer.decorator;

import com.example.exoExplorer.entities.Exoplanet;

/**
 * Concrete component that wraps an Exoplanet entity.
 */
public class BaseExoplanetComponent implements ExoplanetComponent {
    protected final Exoplanet exoplanet;

    public BaseExoplanetComponent(Exoplanet exoplanet) {
        this.exoplanet = exoplanet;
    }

    @Override
    public String getName() {
        return exoplanet.getName();
    }

    @Override
    public Float getDistance() {
        return exoplanet.getDistance();
    }

    @Override
    public Float getTemperature() {
        return exoplanet.getTemperature();
    }

    @Override
    public Integer getYearDiscovered() {
        return exoplanet.getYearDiscovered();
    }

    @Override
    public Float getRadius() {
        return exoplanet.getRadius();
    }

    @Override
    public Float getMass() {
        return exoplanet.getMasse();
    }

    @Override
    public Float getOrbitalPeriod() {
        return exoplanet.getOrbitalPeriodDays();
    }

    @Override
    public String getDescription() {
        return "Exoplanète " + exoplanet.getName();
    }
}
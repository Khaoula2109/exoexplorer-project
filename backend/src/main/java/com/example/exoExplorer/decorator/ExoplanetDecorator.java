package com.example.exoExplorer.decorator;

/**
 * Base decorator class that implements the component interface.
 */
public abstract class ExoplanetDecorator implements ExoplanetComponent {
    protected final ExoplanetComponent decorated;

    public ExoplanetDecorator(ExoplanetComponent decorated) {
        this.decorated = decorated;
    }

    @Override
    public String getName() {
        return decorated.getName();
    }

    @Override
    public Float getDistance() {
        return decorated.getDistance();
    }

    @Override
    public Float getTemperature() {
        return decorated.getTemperature();
    }

    @Override
    public Integer getYearDiscovered() {
        return decorated.getYearDiscovered();
    }

    @Override
    public Float getRadius() {
        return decorated.getRadius();
    }

    @Override
    public Float getMass() {
        return decorated.getMass();
    }

    @Override
    public Float getOrbitalPeriod() {
        return decorated.getOrbitalPeriod();
    }

    @Override
    public String getDescription() {
        return decorated.getDescription();
    }
}
package com.example.exoExplorer.decorator;

/**
 * Decorator that adds Earth comparison information.
 */
public class EarthComparisonDecorator extends ExoplanetDecorator {
    private static final float EARTH_RADIUS = 1.0f; // Earth radius as reference
    private static final float EARTH_MASS = 1.0f;   // Earth mass as reference

    public EarthComparisonDecorator(ExoplanetComponent decorated) {
        super(decorated);
    }

    public String getRadiusComparedToEarth() {
        Float radius = decorated.getRadius();
        if (radius == null) {
            return "Taille inconnue";
        }

        if (Math.abs(radius - EARTH_RADIUS) < 0.1) {
            return "Taille similaire à la Terre";
        } else if (radius > EARTH_RADIUS) {
            return String.format("%.1f fois plus grande que la Terre", radius / EARTH_RADIUS);
        } else {
            return String.format("%.1f fois plus petite que la Terre", EARTH_RADIUS / radius);
        }
    }

    public String getMassComparedToEarth() {
        Float mass = decorated.getMass();
        if (mass == null) {
            return "Masse inconnue";
        }

        if (Math.abs(mass - EARTH_MASS) < 0.1) {
            return "Masse similaire à la Terre";
        } else if (mass > EARTH_MASS) {
            return String.format("%.1f fois plus massive que la Terre", mass / EARTH_MASS);
        } else {
            return String.format("%.1f fois moins massive que la Terre", EARTH_MASS / mass);
        }
    }

    @Override
    public String getDescription() {
        return decorated.getDescription() + " | " + getRadiusComparedToEarth() + " | " + getMassComparedToEarth();
    }
}
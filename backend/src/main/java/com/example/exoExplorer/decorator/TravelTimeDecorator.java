package com.example.exoExplorer.decorator;

/**
 * Decorator that adds travel time calculations.
 */
public class TravelTimeDecorator extends ExoplanetDecorator {
    private final float speedFraction; // As a fraction of light speed

    public TravelTimeDecorator(ExoplanetComponent decorated, float speedFraction) {
        super(decorated);
        this.speedFraction = speedFraction;
    }

    public Float calculateTravelTimeYears() {
        Float distance = decorated.getDistance();
        if (distance == null || speedFraction <= 0) {
            return null;
        }
        return distance / speedFraction;
    }

    @Override
    public String getDescription() {
        String baseDescription = decorated.getDescription();
        Float travelTime = calculateTravelTimeYears();

        if (travelTime == null) {
            return baseDescription + " (temps de voyage non calculable)";
        }

        return baseDescription + String.format(" (temps de voyage estimé: %.1f années à %.1f%% de la vitesse de la lumière)",
                travelTime, speedFraction * 100);
    }
}
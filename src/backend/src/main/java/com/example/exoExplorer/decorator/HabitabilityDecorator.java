package com.example.exoExplorer.decorator;

/**
 * Decorator that adds habitability information.
 */
public class HabitabilityDecorator extends ExoplanetDecorator {
    public HabitabilityDecorator(ExoplanetComponent decorated) {
        super(decorated);
    }

    public boolean isPotentiallyHabitable() {
        Float temp = decorated.getTemperature();
        return temp != null && temp >= 180 && temp <= 310;
    }

    @Override
    public String getDescription() {
        String baseDescription = decorated.getDescription();
        return baseDescription + (isPotentiallyHabitable()
                ? " (potentiellement habitable)"
                : " (non habitable)");
    }
}
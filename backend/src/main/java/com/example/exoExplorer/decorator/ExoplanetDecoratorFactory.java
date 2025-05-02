package com.example.exoExplorer.decorator;

import com.example.exoExplorer.entities.Exoplanet;

/**
 * Factory class to create decorated exoplanet components.
 */
public class ExoplanetDecoratorFactory {
    /**
     * Creates a basic exoplanet component.
     */
    public static ExoplanetComponent createBasic(Exoplanet exoplanet) {
        return new BaseExoplanetComponent(exoplanet);
    }

    /**
     * Creates an exoplanet component with habitability information.
     */
    public static ExoplanetComponent createWithHabitability(Exoplanet exoplanet) {
        return new HabitabilityDecorator(new BaseExoplanetComponent(exoplanet));
    }

    /**
     * Creates an exoplanet component with travel time information.
     */
    public static ExoplanetComponent createWithTravelTime(Exoplanet exoplanet, float speedFraction) {
        return new TravelTimeDecorator(new BaseExoplanetComponent(exoplanet), speedFraction);
    }

    /**
     * Creates an exoplanet component with Earth comparison information.
     */
    public static ExoplanetComponent createWithEarthComparison(Exoplanet exoplanet) {
        return new EarthComparisonDecorator(new BaseExoplanetComponent(exoplanet));
    }

    /**
     * Creates a fully featured exoplanet component with all decorators.
     */
    public static ExoplanetComponent createFullyFeatured(Exoplanet exoplanet, float speedFraction) {
        return new EarthComparisonDecorator(
                new TravelTimeDecorator(
                        new HabitabilityDecorator(
                                new BaseExoplanetComponent(exoplanet)
                        ),
                        speedFraction
                )
        );
    }
}
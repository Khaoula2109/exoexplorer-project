package com.example.exoExplorer.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for application caching.
 * Implements caching to improve performance for frequently accessed data.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Creates a cache manager for the application.
     *
     * @return A CacheManager instance
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                "exoplanetSummaries",
                "exoplanetDetails",
                "userFavorites"
        );
    }
}
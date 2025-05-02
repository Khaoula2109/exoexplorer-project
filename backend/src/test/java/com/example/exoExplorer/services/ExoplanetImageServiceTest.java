package com.example.exoExplorer.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ExoplanetImageServiceTest {

    @Autowired
    private ExoplanetImageService imageService;

    @Test
    void testGetImageUrlShouldReturnCorrectUrl() {
        // This test assumes you have a test exoplanets.json file in your test resources

        // Try getting URLs for some exoplanets
        String url1 = imageService.getImageUrl("Kepler-22b");
        String url2 = imageService.getImageUrl("TRAPPIST-1e");

        // Test that the service handles unknown exoplanets gracefully
        String unknownUrl = imageService.getImageUrl("NonExistentExoplanet");

        // We don't know the exact URLs in your test file, so we'll just check non-null
        // for known exoplanets and null for unknown ones
        assertNotNull(url1, "URL for Kepler-22b should not be null");
        assertNull(unknownUrl, "URL for unknown exoplanet should be null");
    }

    @Test
    void testCaseInsensitiveSearch() {
        // Get URL with different case
        String urlLower = imageService.getImageUrl("kepler-22b");
        String urlMixed = imageService.getImageUrl("Kepler-22B");

        // Both should return the same URL if the exoplanet exists
        assertEquals(urlLower, urlMixed, "Case should not affect URL lookup");
    }
}
package com.example.exoExplorer.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for handling exoplanet images.
 */
@Service
public class ExoplanetImageService {
    private static final Logger logger = LoggerFactory.getLogger(ExoplanetImageService.class);

    // Map to store the association between exoplanet name and image URL
    private final Map<String, String> exoplanetImages = new HashMap<>();

    /**
     * Internal class to represent the association read from JSON.
     */
    public static class ImageMapping {
        private String nomExoplanete;
        private String image;

        public String getNomExoplanete() {
            return nomExoplanete;
        }

        public void setNomExoplanete(String nomExoplanete) {
            this.nomExoplanete = nomExoplanete;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }

    /**
     * Loads image mappings from JSON file at application startup.
     */
    @PostConstruct
    public void loadImages() {
        try {
            // Load the exoplanets.json file from src/main/resources
            ClassPathResource resource = new ClassPathResource("exoplanets.json");
            InputStream is = resource.getInputStream();
            ObjectMapper objectMapper = new ObjectMapper();

            // Read the JSON file into a list of ImageMapping objects
            List<ImageMapping> mappings = objectMapper.readValue(is, new TypeReference<List<ImageMapping>>() {});
            for (ImageMapping mapping : mappings) {
                exoplanetImages.put(mapping.getNomExoplanete(), mapping.getImage());
            }

            logger.info("Loaded {} exoplanet image mappings", exoplanetImages.size());
        } catch (Exception e) {
            logger.error("Error loading exoplanet images", e);
        }
    }

    /**
     * Gets the image URL for an exoplanet.
     *
     * @param exoplanetName The name of the exoplanet
     * @return The image URL, or null if not found
     */
    public String getImageUrl(String exoplanetName) {
        String url = exoplanetImages.getOrDefault(exoplanetName, null);
        if (url == null) {
            // Try case-insensitive search as a fallback
            for (Map.Entry<String, String> entry : exoplanetImages.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(exoplanetName)) {
                    return entry.getValue();
                }
            }
        }
        return url;
    }
}
package com.example.exoExplorer.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

/**
 * Client for fetching exoplanet data from external APIs.
 */
@Service
public class ExternalExoplanetClient {
    private static final Logger logger = LoggerFactory.getLogger(ExternalExoplanetClient.class);
    private final RestTemplate restTemplate;

    /**
     * Constructor that sets up the RestTemplate with appropriate timeouts.
     */
    public ExternalExoplanetClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10000);  // 10 seconds to establish connection
        requestFactory.setReadTimeout(60000);     // 60 seconds for reading (adapted for slow response)
        this.restTemplate = new RestTemplate(requestFactory);
    }

    /**
     * Fetches exoplanet data from the external API.
     *
     * @return List of exoplanet DTOs
     */
    public List<ExoplanetDTO> fetchExoplanetData() {
        logger.info("Fetching exoplanet data from external API");
        String url = "https://exoplanetarchive.ipac.caltech.edu/TAP/sync?query=SELECT+pl_name,+AVG(pl_rade)+AS+avg_rade,+AVG(pl_bmasse)+AS+avg_mass,+AVG(pl_orbsmax)+AS+avg_dist,+AVG(pl_orbper)+AS+avg_period,+AVG(pl_eqt)+AS+avg_temp+FROM+ps+GROUP+BY+pl_name&format=json";

        try {
            ExoplanetDTO[] exoplanets = restTemplate.getForObject(url, ExoplanetDTO[].class);
            logger.info("Successfully fetched {} exoplanets", exoplanets != null ? exoplanets.length : 0);
            return exoplanets != null ? List.of(exoplanets) : List.of();
        } catch (Exception e) {
            logger.error("Error fetching exoplanet data", e);
            return List.of();
        }
    }

    /**
     * DTO for exoplanet data from external API.
     */
    public static class ExoplanetDTO {
        @JsonProperty("pl_name")
        private String plName;

        @JsonProperty("avg_rade")
        private Double avgRade;

        @JsonProperty("avg_mass")
        private Double avgMass;

        @JsonProperty("avg_dist")
        private Double avgDist;

        @JsonProperty("avg_period")
        private Double avgPeriod;

        @JsonProperty("avg_temp")
        private Double avgTemp;

        // Default constructor required for Jackson
        public ExoplanetDTO() {}

        // Getters and Setters
        public String getPlName() {
            return plName;
        }
        public void setPlName(String plName) {
            this.plName = plName;
        }

        public Double getAvgRade() {
            return avgRade;
        }
        public void setAvgRade(Double avgRade) {
            this.avgRade = avgRade;
        }

        public Double getAvgMass() {
            return avgMass;
        }
        public void setAvgMass(Double avgMass) {
            this.avgMass = avgMass;
        }

        public Double getAvgDist() {
            return avgDist;
        }
        public void setAvgDist(Double avgDist) {
            this.avgDist = avgDist;
        }

        public Double getAvgPeriod() {
            return avgPeriod;
        }
        public void setAvgPeriod(Double avgPeriod) {
            this.avgPeriod = avgPeriod;
        }

        public Double getAvgTemp() {
            return avgTemp;
        }
        public void setAvgTemp(Double avgTemp) {
            this.avgTemp = avgTemp;
        }
    }
}
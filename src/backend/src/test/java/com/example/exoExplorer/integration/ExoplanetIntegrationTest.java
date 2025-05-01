package com.example.exoExplorer.integration;

import com.example.exoExplorer.entities.Exoplanet;
import com.example.exoExplorer.repositories.ExoplaneteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExoplanetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExoplaneteRepository exoplanetRepository;

    private String insertedName;
    private Integer insertedId;

    @BeforeEach
    void setup() {
        exoplanetRepository.deleteAll();

        insertedName = "Kepler-" + System.currentTimeMillis();
        Exoplanet exo = new Exoplanet();
        exo.setName(insertedName);
        exoplanetRepository.save(exo);

        insertedId = exo.getId();
    }

    @Test
    void testGetAllExoplanets() throws Exception {
        mockMvc.perform(get("/api/exoplanets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(insertedName));
    }

    @Test
    void testGetExoplanetById() throws Exception {
        mockMvc.perform(get("/api/exoplanets/" + insertedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(insertedName));
    }
}

package com.example.exoExplorer.controllers;

import com.example.exoExplorer.dto.ExoplanetSummaryDTO;
import com.example.exoExplorer.entities.Exoplanet;
import com.example.exoExplorer.repositories.ExoplaneteRepository;
import com.example.exoExplorer.services.ExoplanetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ExoplanetControllerTest {

    @InjectMocks
    private ExoplanetController exoplanetController;

    @Mock
    private ExoplanetService exoplanetService;

    @Mock
    private ExoplaneteRepository exoplanetRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllExoplanets() {
        // Arrange
        List<Exoplanet> mockList = List.of(new Exoplanet(), new Exoplanet());
        when(exoplanetService.getAllExoplanets()).thenReturn(mockList);

        // Act
        ResponseEntity<List<Exoplanet>> response = exoplanetController.getAllExoplanets();

        // Assert
        assertThat(response.getBody()).hasSize(2);
        verify(exoplanetService, times(1)).getAllExoplanets();
    }

    @Test
    void testGetExoplanetById_Found() {
        // Arrange
        Exoplanet mockExo = new Exoplanet();
        mockExo.setId(1);
        when(exoplanetService.getExoplanetById(1)).thenReturn(mockExo);

        // Act
        ResponseEntity<Exoplanet> response = exoplanetController.getExoplanetById(1);

        // Assert
        assertThat(response.getBody()).isEqualTo(mockExo);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    void testRefreshData() {
        // Act
        ResponseEntity<String> response = exoplanetController.refreshData();

        // Assert
        verify(exoplanetService, times(1)).refreshExoplanetData();
        assertThat(response.getBody()).isEqualTo("Exoplanet data refreshed successfully");
    }

    @Test
    void testGetExoplanetSummaries_shouldReturnPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<ExoplanetSummaryDTO> mockPage = new PageImpl<>(List.of(
                new ExoplanetSummaryDTO(1, "PlanetA", "imgA"),
                new ExoplanetSummaryDTO(2, "PlanetB", "imgB")
        ));

        when(exoplanetService.getExoplanetSummaries(any(Specification.class), eq(pageable)))
                .thenReturn(mockPage);

        // Act
        ResponseEntity<Page<ExoplanetSummaryDTO>> response = exoplanetController.getExoplanetSummaries(
                null, null, null, null, null, null, null, pageable
        );

        // Assert
        assertThat(response.getBody().getTotalElements()).isEqualTo(2);
        assertThat(response.getBody().getContent().get(0).getName()).isEqualTo("PlanetA");
        assertThat(response.getBody().getContent().get(1).getImageExo()).isEqualTo("imgB");

        verify(exoplanetService, times(1)).getExoplanetSummaries(any(Specification.class), eq(pageable));
    }
}
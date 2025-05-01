package com.example.exoExplorer.services;

import com.example.exoExplorer.entities.Exoplanet;
import com.example.exoExplorer.repositories.ExoplaneteRepository;
import com.example.exoExplorer.services.ExternalExoplanetClient.ExoplanetDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ExoplanetServiceTest {

    @Mock
    private ExoplaneteRepository exoplanetRepository;

    @Mock
    private ExternalExoplanetClient externalClient;

    @Mock
    private ExoplanetImageService imageService;

    @InjectMocks
    private ExoplanetService exoplanetService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRefreshExoplanetData_withNewData_shouldSaveNewExoplanet() {
        // GIVEN
        ExoplanetDTO dto = new ExoplanetDTO();
        dto.setPlName("Kepler-22b");
        dto.setAvgRade(2.4);
        dto.setAvgMass(5.5);
        dto.setAvgDist(600.0);
        dto.setAvgPeriod(290.0);
        dto.setAvgTemp(295.0);

        when(externalClient.fetchExoplanetData()).thenReturn(List.of(dto));
        when(exoplanetRepository.findByNameIgnoreCase("Kepler-22b")).thenReturn(Optional.empty());
        when(imageService.getImageUrl("Kepler-22b")).thenReturn("https://example.com/kepler.jpg");

        // WHEN
        exoplanetService.refreshExoplanetData();

        // THEN
        ArgumentCaptor<Exoplanet> captor = ArgumentCaptor.forClass(Exoplanet.class);
        verify(exoplanetRepository, times(1)).save(captor.capture());

        Exoplanet saved = captor.getValue();
        assertEquals("Kepler-22b", saved.getName());
        assertEquals(2.4f, saved.getRadius());
        assertEquals(5.5f, saved.getMasse());
        assertEquals(600.0f, saved.getDistance());
        assertEquals(290.0f, saved.getOrbitalPeriodDays());
        assertEquals(290.0f / 365.0f, saved.getOrbitalPeriodYear());
        assertEquals(295.0f, saved.getTemperature());
        assertEquals("https://example.com/kepler.jpg", saved.getImageExo());
    }

    @Test
    void testRefreshExoplanetData_withExistingData_shouldUpdateExoplanet() {
        // GIVEN
        Exoplanet existing = new Exoplanet();
        existing.setName("Kepler-22b");

        ExoplanetDTO dto = new ExoplanetDTO();
        dto.setPlName("Kepler-22b");
        dto.setAvgRade(3.1);
        dto.setAvgMass(7.1);
        dto.setAvgDist(1200.0);
        dto.setAvgPeriod(310.0);
        dto.setAvgTemp(280.0);

        // Préparation des mocks
        when(externalClient.fetchExoplanetData()).thenReturn(List.of(dto));
        when(exoplanetRepository.findByNameIgnoreCase("Kepler-22b")).thenReturn(Optional.of(existing));

        // WHEN
        exoplanetService.refreshExoplanetData();

        // THEN - En utilisant un ArgumentCaptor pour capturer l'instance sauvegardée
        ArgumentCaptor<Exoplanet> exoplanetCaptor = ArgumentCaptor.forClass(Exoplanet.class);
        verify(exoplanetRepository).save(exoplanetCaptor.capture());

        // Vérifier les propriétés de l'exoplanète sauvegardée
        Exoplanet savedExoplanet = exoplanetCaptor.getValue();
        assertEquals("Kepler-22b", savedExoplanet.getName());
        assertEquals(3.1f, savedExoplanet.getRadius(), 0.001);
        assertEquals(7.1f, savedExoplanet.getMasse(), 0.001);
        assertEquals(1200.0f, savedExoplanet.getDistance(), 0.001);
        assertEquals(310.0f, savedExoplanet.getOrbitalPeriodDays(), 0.001);
        assertEquals(310.0f / 365.0f, savedExoplanet.getOrbitalPeriodYear(), 0.001);
        assertEquals(280.0f, savedExoplanet.getTemperature(), 0.001);
    }

    @Test
    void testGetExoplanetById_Found() {
        // GIVEN
        Exoplanet exoplanet = new Exoplanet();
        exoplanet.setId(1);
        exoplanet.setName("Test Exoplanet");

        when(exoplanetRepository.findById(1)).thenReturn(Optional.of(exoplanet));

        // WHEN
        Exoplanet result = exoplanetService.getExoplanetById(1);

        // THEN
        assertNotNull(result);
        assertEquals("Test Exoplanet", result.getName());
        verify(exoplanetRepository).findById(1);
    }

    @Test
    void testGetAllExoplanets() {
        // GIVEN
        List<Exoplanet> exoplanets = List.of(
                new Exoplanet(), new Exoplanet()
        );
        when(exoplanetRepository.findAll()).thenReturn(exoplanets);

        // WHEN
        List<Exoplanet> result = exoplanetService.getAllExoplanets();

        // THEN
        assertEquals(2, result.size());
        verify(exoplanetRepository).findAll();
    }
}
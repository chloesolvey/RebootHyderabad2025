package com.lloyds.onboard.service;

import com.lloyds.onboard.entity.JourneyTemplate;
import com.lloyds.onboard.repository.JourneyTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JourneyTemplateServiceTest {

    @Mock
    private JourneyTemplateRepository journeyRepo;

    @InjectMocks
    private JourneyTemplateService service;

    @Test
    void shouldReturnAllJourneys() {
        List<JourneyTemplate> journeys = List.of(new JourneyTemplate(), new JourneyTemplate());
        when(journeyRepo.findAll()).thenReturn(journeys);

        List<JourneyTemplate> result = service.getAllJourneys();

        assertEquals(2, result.size());
        verify(journeyRepo).findAll();
    }

    @Test
    void shouldReturnJourneyById() {
        JourneyTemplate journey = new JourneyTemplate();
        journey.setId(100L);
        when(journeyRepo.findById(100L)).thenReturn(Optional.of(journey));

        Optional<JourneyTemplate> result = service.getJourneyById(100L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getId());
        verify(journeyRepo).findById(100L);
    }

    @Test
    void shouldCreateJourney() {
        JourneyTemplate journey = new JourneyTemplate();
        when(journeyRepo.save(journey)).thenReturn(journey);

        JourneyTemplate result = service.createJourney(journey);

        assertNotNull(result);
        verify(journeyRepo).save(journey);
    }

    @Test
    void shouldUpdateJourneyIfExists() {
        JourneyTemplate existing = new JourneyTemplate();
        existing.setId(200L);
        existing.setJourneytype("Old");
        existing.setVersion("v1");

        JourneyTemplate updated = new JourneyTemplate();
        updated.setJourneytype("New");
        updated.setVersion("v2");
        updated.setTemplatedata("Updated Data");

        when(journeyRepo.findById(200L)).thenReturn(Optional.of(existing));
        when(journeyRepo.save(any())).thenReturn(existing);

        Optional<JourneyTemplate> result = service.updateJourney(200L, updated);

        assertTrue(result.isPresent());
        assertEquals("New", result.get().getJourneytype());
        assertEquals("v2", result.get().getVersion());
        verify(journeyRepo).save(existing);
    }

    @Test
    void shouldReturnEmptyWhenUpdatingNonexistentJourney() {
        JourneyTemplate updated = new JourneyTemplate();
        when(journeyRepo.findById(300L)).thenReturn(Optional.empty());

        Optional<JourneyTemplate> result = service.updateJourney(300L, updated);

        assertTrue(result.isEmpty());
        verify(journeyRepo, never()).save(any());
    }

    @Test
    void shouldDeleteJourneyIfExists() {
        when(journeyRepo.existsById(400L)).thenReturn(true);

        boolean result = service.deleteJourney(400L);

        assertTrue(result);
        verify(journeyRepo).deleteById(400L);
    }

    @Test
    void shouldNotDeleteJourneyIfNotExists() {
        when(journeyRepo.existsById(500L)).thenReturn(false);

        boolean result = service.deleteJourney(500L);

        assertFalse(result);
        verify(journeyRepo, never()).deleteById(any());
    }
}
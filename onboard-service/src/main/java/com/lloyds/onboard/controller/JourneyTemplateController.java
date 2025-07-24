package com.lloyds.onboard.controller;

import com.lloyds.onboard.entity.JourneyTemplate;
import com.lloyds.onboard.service.JourneyTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing JourneyTemplate entities.
 * Provides endpoints for retrieving, creating, updating, and deleting journey templates.
 */
@RestController
@RequestMapping("/api/journeys")
public class JourneyTemplateController {

    @Autowired
    private JourneyTemplateService journeyService;

    /**
     * Retrieves all journey templates.
     *
     * @return a list of JourneyTemplate entities
     */
    @GetMapping
    public List<JourneyTemplate> getAllJourneys() {
        return journeyService.getAllJourneys();
    }

    /**
     * Creates a new journey template.
     *
     * @param journey the JourneyTemplate to create
     * @return the created JourneyTemplate entity
     */
    @PostMapping
    public JourneyTemplate createJourney(@RequestBody JourneyTemplate journey) {
        return journeyService.createJourney(journey);
    }

    /**
     * Updates an existing journey template by ID.
     *
     * @param id the ID of the journey template to update
     * @param updated the updated JourneyTemplate data
     * @return ResponseEntity with the updated JourneyTemplate, or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<JourneyTemplate> updateJourney(@PathVariable Long id, @RequestBody JourneyTemplate updated) {
        return journeyService.updateJourney(id, updated)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a journey template by ID.
     *
     * @param id the ID of the journey template to delete
     * @return ResponseEntity with status 200 if deleted, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJourney(@PathVariable Long id) {
        boolean deleted = journeyService.deleteJourney(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
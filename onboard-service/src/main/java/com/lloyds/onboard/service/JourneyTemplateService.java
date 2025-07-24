package com.lloyds.onboard.service;

import com.lloyds.onboard.entity.JourneyTemplate;
import com.lloyds.onboard.repository.JourneyTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing JourneyTemplate entities.
 * <p>
 * Provides methods to create, retrieve, update, and delete journey templates.
 * </p>
 */
@Service
public class JourneyTemplateService {

    @Autowired
    private JourneyTemplateRepository journeyRepo;

    /**
     * Retrieves all journey templates.
     *
     * @return a list of all {@link JourneyTemplate} entities
     */
    public List<JourneyTemplate> getAllJourneys() {
        return journeyRepo.findAll();
    }

    /**
     * Retrieves a journey template by its ID.
     *
     * @param id the ID of the journey template
     * @return an {@link Optional} containing the {@link JourneyTemplate} if found, or empty otherwise
     */
    public Optional<JourneyTemplate> getJourneyById(Long id) {
        return journeyRepo.findById(id);
    }

    /**
     * Creates a new journey template.
     *
     * @param journey the {@link JourneyTemplate} entity to create
     * @return the created {@link JourneyTemplate} entity
     */
    public JourneyTemplate createJourney(JourneyTemplate journey) {
        return journeyRepo.save(journey);
    }

    /**
     * Updates an existing journey template by its ID.
     * <p>
     * If a journey template with the given ID exists, updates its fields with the provided data.
     * </p>
     *
     * @param id      the ID of the journey template to update
     * @param updated the updated journey template data
     * @return an {@link Optional} containing the updated {@link JourneyTemplate} if found, or empty otherwise
     */
    public Optional<JourneyTemplate> updateJourney(Long id, JourneyTemplate updated) {
        return journeyRepo.findById(id).map(existing -> {
            existing.setJourneytype(updated.getJourneytype());
            existing.setVersion(updated.getVersion());
            existing.setTemplatedata(updated.getTemplatedata());
            return journeyRepo.save(existing);
        });
    }

    /**
     * Deletes a journey template by its ID.
     *
     * @param id the ID of the journey template to delete
     * @return true if the journey template was found and deleted, false otherwise
     */
    public boolean deleteJourney(Long id) {
        if (journeyRepo.existsById(id)) {
            journeyRepo.deleteById(id);
            return true;
        }
        return false;
    }
}

package com.lloyds.onboard.service;

import com.lloyds.onboard.entity.CustomerFeedback;
import com.lloyds.onboard.repository.CustomerFeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing customer feedback.
 * <p>
 * Provides methods to save customer feedback and retrieve feedback related to a specific application.
 * </p>
 */
@Service
public class CustomerFeedbackService {

    private final CustomerFeedbackRepository repository;

    /**
     * Constructs a CustomerFeedbackService with the specified repository.
     *
     * @param repository the repository used for CRUD operations on CustomerFeedback entities
     */
    public CustomerFeedbackService(CustomerFeedbackRepository repository) {
        this.repository = repository;
    }

    /**
     * Saves a new customer feedback entry.
     *
     * @param feedback the {@link CustomerFeedback} entity to save
     * @return the saved {@link CustomerFeedback} entity
     */
    public CustomerFeedback saveFeedback(CustomerFeedback feedback) {
        return repository.save(feedback);
    }

    /**
     * Retrieves a list of customer feedback for a given application ID,
     * ordered by creation date in descending order.
     *
     * @param applicationid the ID of the application for which feedback is requested
     * @return a list of {@link CustomerFeedback} entities
     */
    public List<CustomerFeedback> getFeedbackByApplicationId(Long applicationid) {
        return repository.findByApplicationidOrderByCreateddateDesc(applicationid);
    }
}

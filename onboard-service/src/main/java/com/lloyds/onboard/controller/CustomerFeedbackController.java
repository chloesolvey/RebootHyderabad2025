package com.lloyds.onboard.controller;

import com.lloyds.onboard.entity.CustomerFeedback;
import com.lloyds.onboard.service.CustomerFeedbackService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for handling customer feedback operations.
 * Provides endpoints to submit and retrieve feedback based on application ID.
 */
@RestController
@RequestMapping("/api/customerfeedback")
public class CustomerFeedbackController {

    private final CustomerFeedbackService service;

    /**
     * Constructs a new CustomerFeedbackController with the provided service.
     * @param service the customer feedback service
     */
    public CustomerFeedbackController(CustomerFeedbackService service) {
        this.service = service;
    }

    /**
     * Submits new customer feedback.
     *
     * @param feedback the feedback entity received in the request body
     * @return the saved CustomerFeedback object
     */
    @PostMapping
    public CustomerFeedback submitFeedback(@RequestBody CustomerFeedback feedback) {
        return service.saveFeedback(feedback);
    }

    /**
     * Retrieves all customer feedback associated with a specific application ID.
     *
     * @param applicationid the unique identifier for the application
     * @return a list of CustomerFeedback entries related to the given application ID
     */
    @GetMapping("/{applicationid}")
    public List<CustomerFeedback> getFeedbackByAppId(@PathVariable Long applicationid) {
        return service.getFeedbackByApplicationId(applicationid);
    }
}

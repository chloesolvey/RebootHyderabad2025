package com.lloyds.onboard.controller;

import com.lloyds.onboard.entity.Application;
import com.lloyds.onboard.entity.ResumeApplication;
import com.lloyds.onboard.model.Constants;
import com.lloyds.onboard.model.NotificationType;
import com.lloyds.onboard.service.ApplicationService;
import com.lloyds.onboard.service.notification.NotificationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

/**
 * REST controller for managing Application entities and related workflows.
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService service;
    private final Map<String, NotificationService> notificationServices;

    /**
     * Constructs the ApplicationController with required service dependencies.
     * @param service the application service
     * @param notificationServices a map of available notification services
     */
    public ApplicationController(ApplicationService service, Map<String, NotificationService> notificationServices) {
        this.service = service;
        this.notificationServices = notificationServices;
    }

    /**
     * Retrieves all applications or filters by RM ID.
     * @param rmid optional relationship manager ID
     * @return list of applications
     */
    @GetMapping
    public List<Application> getAll(@RequestParam(required = false) String rmid) {
        if (rmid != null && !rmid.isEmpty()) {
            return service.getApplicationsByRmid(rmid);
        }
        return service.getAllApplications();
    }

    /**
     * Retrieves a specific application by its ID.
     * @param applicationId the application ID
     * @return the application object
     */
    @GetMapping("/{applicationId}")
    public Application getApplication(@PathVariable Long applicationId) {
        return service.getApplication(applicationId);
    }

    /**
     * Creates a new application entry.
     * @param app the application payload
     * @return the created application
     */
    @PostMapping
    public Application create(@RequestBody Application app) {
        return service.createApplication(app);
    }

    /**
     * Saves and continues the application, optionally sending notifications.
     * @param app the application data
     * @param headers HTTP request headers
     * @return the updated application
     * @throws Exception if notification fails
     */
    @PutMapping
    public Application saveAndContinue(@RequestBody Application app, @RequestHeader Map<String, String> headers) throws Exception {
        String sessionStatus = headers.get(Constants.SESSION_STATUS);
        if (sessionStatus == null || !sessionStatus.equals(Constants.IN_PROGRESS)) {
            notificationServices.get(NotificationType.MAIL.getServiceName()).sendNotification(app.getAppid(), app.getEmail(), app.getFirstname(), app.getJourneytype());
           // notificationServices.get(NotificationType.SMS.getServiceName()).sendNotification(app.getAppid(), app.getMobilenumber());
        }
        return service.updateApplication(app.getAppid(), app);
    }

    /**
     * Submits an application and returns a redirect response.
     * @param app the application data
     * @return redirection to a confirmation page
     */
    @PostMapping("/submit-application")
    public ResponseEntity<String> submitApplication(@RequestBody Application app) {
        service.updateApplication(app.getAppid(), app);
        URI uri = URI.create("http://localhost:8080/PCAAccounts.html");
        return ResponseEntity.ok(uri.toString());
        //return ResponseEntity.status(HttpStatus.SEE_OTHER).location(uri).build();
    }

    /**
     * Resumes an application journey using a token.
     * @param token encrypted application token
     * @return resume application response
     * @throws Exception if token is invalid or decryption fails
     */
    @GetMapping("/resume-journey")
    public ResponseEntity<ResumeApplication> resumeJourney(@RequestParam(value = "token") String token) throws Exception {
        return ok(service.resumeJourney(token));
    }

    /**
     * Assigns the application to a relationship manager.
     * @param applicationId the application ID
     * @param req contains the RM assignment info
     * @return the updated application
     */
    @PutMapping("/{applicationId}/assign")
    public Application assignRM(@PathVariable Long applicationId, @RequestBody Map<String,String> req) {
        return service.assignToRM(applicationId, req.get("assignedTo"));
    }

    /**
     * Updates the application identified by app ID.
     * @param appid the application ID
     * @param updated the updated application payload
     * @return the updated application
     */
    @PutMapping("/{appid}")
    public Application update(@PathVariable String appid, @RequestBody Application updated) {
        return service.updateApplication(appid, updated);
    }

    /**
     * Deletes an application by its ID.
     * @param id the application ID to delete
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteApplication(id);
    }


    /**
     * Simple request object for assigning RM.
     */
    public static class AssignRequest {
        private String assignedTo;

        /**
         * Gets the RM ID.
         * @return the RM ID
         */
        public String getRmid() { return assignedTo; }

        /**
         * Sets the RM ID.
         * @param rmid the RM ID to assign
         */
        public void setRmid(String rmid) { this.assignedTo = rmid; }
    }
}

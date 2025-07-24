package com.lloyds.onboard.controller;

import com.lloyds.onboard.entity.RmAudit;
import com.lloyds.onboard.service.RmAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing RM Audit actions.
 * <p>
 * Provides endpoints to log audit actions and retrieve audit history for a given application ID.
 * </p>
 * Base URL mapping for this controller is "/api/rmaudit".
 */
@RestController
@RequestMapping("/api/rmaudit")
@Slf4j
public class RmAuditController {

    private final RmAuditService service;

    /**
     * Constructs an RmAuditController with the specified {@link RmAuditService}.
     *
     * @param service the service handling RM audit operations
     */
    public RmAuditController(RmAuditService service) {
        this.service = service;
    }

    /**
     * Logs a new RM audit action.
     * <p>
     * Accepts a POST request with an {@link RmAudit} object representing the audit to be logged.
     * Returns the saved audit entity.
     * </p>
     *
     * @param audit the RM audit entity to be logged
     * @return a ResponseEntity containing the created {@link RmAudit}
     */
    @PostMapping
    public ResponseEntity<RmAudit> logAction(@RequestBody RmAudit audit) {
        return ResponseEntity.ok(service.createAudit(audit));
    }

    /**
     * Retrieves the audit history for the specified application ID.
     * <p>
     * Accepts a GET request with the application ID as a path variable.
     * Returns a list of {@link RmAudit} records related to that application.
     * </p>
     *
     * @param applicationId the ID of the application for which audit history is requested
     * @return a list of {@link RmAudit} records associated with the application
     */
    @GetMapping("/{applicationId}")
    public List<RmAudit> getApplication(@PathVariable Long applicationId) {
        return service.getAuditHistory(applicationId);
    }
}

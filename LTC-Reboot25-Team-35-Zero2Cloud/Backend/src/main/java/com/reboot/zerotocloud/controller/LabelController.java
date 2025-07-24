package com.reboot.zerotocloud.controller;

import com.reboot.zerotocloud.dto.TeamRegistrationRequest;
import com.reboot.zerotocloud.service.GCPLabelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gcp/labels")
@CrossOrigin(origins = "*")
public class LabelController {

    @Autowired
    private GCPLabelService gcpLabelService;

    @PostMapping("/register-team")
    public ResponseEntity<String> registerTeam(@RequestBody TeamRegistrationRequest request) {
        try {
            boolean success = gcpLabelService.addTeamLabel(request.getProjectId(), request.getLabelValue());

            if (!success) {
                return ResponseEntity.badRequest()
                        .body("Label already exists for project: " + request.getProjectId());
            }

            return ResponseEntity.ok("Label registered successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to register label: " + e.getMessage());
        }
    }

    // ... rest of the controller methods ...
}
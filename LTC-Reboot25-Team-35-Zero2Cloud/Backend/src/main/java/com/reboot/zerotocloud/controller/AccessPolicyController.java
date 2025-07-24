package com.reboot.zerotocloud.controller;

import com.reboot.zerotocloud.dto.AccessPolicyDTO;
import com.reboot.zerotocloud.service.AccessPolicyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/access-policies")
@CrossOrigin(origins = "*")
public class AccessPolicyController {

    @Autowired
    private AccessPolicyService accessPolicyService;

    @Value("${gcp.project.id}")
    private String projectId;

    @GetMapping
    public ResponseEntity<List<AccessPolicyDTO>> getAllPolicies() {
        return ResponseEntity.ok(accessPolicyService.getAllPolicies(projectId));
    }
}
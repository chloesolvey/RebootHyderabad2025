package com.reboot.zerotocloud.controller;

import com.reboot.zerotocloud.model.VMProvisionCriteria;
import com.reboot.zerotocloud.service.GCPProvisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.google.cloud.ServiceOptions;


@RestController
@RequestMapping("/provisioning")
@CrossOrigin(origins = "*") // Allows all origins
public class ProvisionController {

	@Autowired
	private GCPProvisionService gcpProvisionService;

	@PostMapping("/vm")
	public ResponseEntity<String> provisionVM(@RequestBody VMProvisionCriteria vmprovision) {
		try {
			gcpProvisionService.provisionVM(vmprovision);
			return ResponseEntity.ok("VM provisioning initiated successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError()
					.body("Failed to provision VM: " + e.getMessage());
		}
	}

	@DeleteMapping("/vm/{zone}/{name}")
	public ResponseEntity<String> deleteVM(
			@PathVariable String zone,
			@PathVariable String name,
			@RequestParam String projectId) {  // Remove optional and make it required
		try {
			gcpProvisionService.deleteVM(projectId, zone, name);
			return ResponseEntity.ok("VM deletion initiated successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError()
					.body("Failed to delete VM: " + e.getMessage());
		}
	}
}
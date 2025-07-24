package com.reboot.zerotocloud.service;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.compute.v1.*;
import com.reboot.zerotocloud.model.VMProvisionCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

@Service
public class GCPProvisionService {

    @Autowired
    private GCPLabelService gcpLabelService;

    public Operation provisionVM(VMProvisionCriteria criteria) throws IOException, ExecutionException, InterruptedException {
        // First, validate that the tag matches an existing team label
        Map<String, String> projectLabels = gcpLabelService.getProjectLabels(criteria.getProjectId());
        String teamLabel = projectLabels.get("team");

        if (teamLabel == null) {
            throw new IllegalStateException("No team label found for project: " + criteria.getProjectId());
        }

        if (!teamLabel.equals(criteria.getTags())) {
            throw new IllegalStateException("Provided tag '" + criteria.getTags() +
                    "' does not match the registered team label '" + teamLabel + "'");
        }

        try (InstancesClient instancesClient = InstancesClient.create()) {
            // Create labels map for the instance
            Map<String, String> labels = new HashMap<>();
            labels.put("team", teamLabel);

            Instance instanceResource = Instance.newBuilder()
                    .setName(criteria.getInstanceName())
                    .setMachineType(String.format("zones/%s/machineTypes/%s",
                            criteria.getZone(),
                            criteria.getMachineType()))
                    .addNetworkInterfaces(NetworkInterface.newBuilder()
                            .setName("default")
                            .build())
                    .addDisks(AttachedDisk.newBuilder()
                            .setInitializeParams(AttachedDiskInitializeParams.newBuilder()
                                    .setSourceImage("projects/debian-cloud/global/images/debian-11-bullseye-v20240213")
                                    .build())
                            .setAutoDelete(true)
                            .setBoot(true)
                            .setType("PERSISTENT")
                            .build())
                    .putAllLabels(labels)  // Use putAllLabels instead of setLabels
                    .setMetadata(Metadata.newBuilder()
                            .addItems(Items.newBuilder()
                                    .setKey("startup-script")
                                    .setValue("#!/bin/bash\n" +
                                            "curl -sSO https://dl.google.com/cloudagents/add-google-cloud-ops-agent-repo.sh\n" +
                                            "sudo bash add-google-cloud-ops-agent-repo.sh --also-install\n"))
                            .build())

                    .build();

            InsertInstanceRequest request = InsertInstanceRequest.newBuilder()
                    .setProject(criteria.getProjectId())
                    .setZone(criteria.getZone())
                    .setInstanceResource(instanceResource)
                    .build();

            return instancesClient.insertAsync(request).get();
        }
    }

    public Operation deleteVM(String projectId, String zone, String instanceName) throws IOException, InterruptedException, ExecutionException {
        try {
            // First verify if the VM exists and get its labels
            try (InstancesClient instancesClient = InstancesClient.create()) {
                Instance instance = instancesClient.get(projectId, zone, instanceName);

                // Get project team label
                Map<String, String> projectLabels = gcpLabelService.getProjectLabels(projectId);
                String projectTeamLabel = projectLabels.get("team");

                // Get instance team label
                String instanceTeamLabel = instance.getLabelsMap().get("team");

                // Verify that the instance has the same team label as the project
                if (!projectTeamLabel.equals(instanceTeamLabel)) {
                    throw new IllegalStateException("Cannot delete VM: Instance team label does not match project team label");
                }

                DeleteInstanceRequest deleteInstanceRequest = DeleteInstanceRequest.newBuilder()
                        .setProject(projectId)
                        .setZone(zone)
                        .setInstance(instanceName)
                        .build();

                return instancesClient.deleteAsync(deleteInstanceRequest).get();
            }
        } catch (NotFoundException e) {
            throw new IllegalStateException("VM not found: " + instanceName);
        }
    }

    /**
     * Validates if a machine type is available in the specified zone
     */
    private void validateMachineType(String projectId, String zone, String machineType) throws IOException {
        try (MachineTypesClient machineTypesClient = MachineTypesClient.create()) {
            try {
                machineTypesClient.get(projectId, zone, machineType);
            } catch (NotFoundException e) {
                throw new IllegalArgumentException(
                        String.format("Machine type %s is not available in zone %s", machineType, zone));
            }
        }
    }
}
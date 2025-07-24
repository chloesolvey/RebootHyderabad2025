package com.reboot.zerotocloud.service;

import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

@Service
public class GCPLabelService {

    private final ResourceManager resourceManager;

    public GCPLabelService() {
        this.resourceManager = ResourceManagerOptions.getDefaultInstance().getService();
    }

    public boolean addTeamLabel(String projectId, String labelValue) {
        try {
            Project project = resourceManager.get(projectId);
            if (project == null) {
                throw new RuntimeException("Project not found: " + projectId);
            }

            Map<String, String> labels = new HashMap<>(project.getLabels() != null ? project.getLabels() : new HashMap<>());

            // Check if team label already exists
            if (labels.containsKey("team")) {
                return false;
            }

            // Add new label
            labels.put("team", labelValue);

            // Create project update request
            Project updatedProject = project.toBuilder()
                    .setLabels(labels)
                    .build();

            // Submit the update request
            Project result = resourceManager.replace(updatedProject);
            return result != null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to add team label: " + e.getMessage(), e);
        }
    }

    public Map<String, String> getProjectLabels(String projectId) {
        try {
            Project project = resourceManager.get(projectId);
            if (project == null) {
                throw new RuntimeException("Project not found: " + projectId);
            }
            return project.getLabels();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get project labels: " + e.getMessage(), e);
        }
    }

    public boolean removeTeamLabel(String projectId) {
        try {
            Project project = resourceManager.get(projectId);
            if (project == null) {
                throw new RuntimeException("Project not found: " + projectId);
            }

            Map<String, String> labels = new HashMap<>(project.getLabels() != null ? project.getLabels() : new HashMap<>());
            if (!labels.containsKey("team")) {
                return false;
            }

            labels.remove("team");

            Project updatedProject = project.toBuilder()
                    .setLabels(labels)
                    .build();

            Project result = resourceManager.replace(updatedProject);
            return result != null;

        } catch (Exception e) {
            throw new RuntimeException("Failed to remove team label: " + e.getMessage(), e);
        }
    }
}
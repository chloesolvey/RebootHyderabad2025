package com.reboot.zerotocloud.service;

import com.google.cloud.resourcemanager.v3.Project;
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import com.google.iam.v1.Binding;
import com.google.iam.v1.GetIamPolicyRequest;
import com.google.iam.v1.Policy;
import com.reboot.zerotocloud.dto.AccessPolicyDTO;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AccessPolicyService {

    public List<AccessPolicyDTO> getAllPolicies(String projectId) {
        try (ProjectsClient projectsClient = ProjectsClient.create()) {
            GetIamPolicyRequest request = GetIamPolicyRequest.newBuilder()
                    .setResource("projects/" + projectId)
                    .build();

            Policy policy = projectsClient.getIamPolicy(request);
            return convertBindingsToDTO(policy.getBindingsList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch IAM policies", e);
        }
    }

    private List<AccessPolicyDTO> convertBindingsToDTO(List<Binding> bindings) {
        List<AccessPolicyDTO> policies = new ArrayList<>();

        for (Binding binding : bindings) {
            AccessPolicyDTO dto = new AccessPolicyDTO();
            dto.setRole(binding.getRole());
            dto.setMembers(new HashSet<>(binding.getMembersList()));
            dto.setScope("Project");
            dto.setDescription(generateDescription(binding.getRole()));
            policies.add(dto);
        }

        return policies;
    }

    private String generateDescription(String role) {
        // Generate a human-readable description based on the role
        String[] parts = role.split("\\.");
        String roleName = parts[parts.length - 1];
        return String.format("%s access role",
                roleName.substring(0, 1).toUpperCase() + roleName.substring(1));
    }
}
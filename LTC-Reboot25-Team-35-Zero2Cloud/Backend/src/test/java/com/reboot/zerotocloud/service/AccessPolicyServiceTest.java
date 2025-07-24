package com.reboot.zerotocloud.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.google.cloud.resourcemanager.v3.ProjectsClient;
import com.google.iam.v1.Binding;
import com.google.iam.v1.GetIamPolicyRequest;
import com.google.iam.v1.Policy;
import com.reboot.zerotocloud.dto.AccessPolicyDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class AccessPolicyServiceTest {

    @Test
    void getAllPolicies_successfulConversion() throws Exception {
        // Prepare two IAM bindings
        Binding binding1 = Binding.newBuilder()
                .setRole("roles/viewer")
                .addMembers("user:alice@example.com")
                .addMembers("serviceAccount:svc@example.com")
                .build();

        Binding binding2 = Binding.newBuilder()
                .setRole("roles/editor")
                .addMembers("group:devs@example.com")
                .build();

        Policy fakePolicy = Policy.newBuilder()
                .addBindings(binding1)
                .addBindings(binding2)
                .build();

        // Mock ProjectsClient.create() and its getIamPolicy(...) call
        try (MockedStatic<ProjectsClient> projectsClientMock = mockStatic(ProjectsClient.class)) {
            ProjectsClient client = mock(ProjectsClient.class);
            projectsClientMock.when(ProjectsClient::create).thenReturn(client);

            // Ensure getIamPolicy returns our fake policy
            when(client.getIamPolicy(any(GetIamPolicyRequest.class)))
                    .thenReturn(fakePolicy);

            // Invoke service
            AccessPolicyService service = new AccessPolicyService();
            List<AccessPolicyDTO> dtos = service.getAllPolicies("my-project");

            // Validate two DTOs returned
            assertEquals(2, dtos.size());

            // First DTO assertions
            AccessPolicyDTO dto1 = dtos.stream()
                    .filter(d -> d.getRole().equals("roles/viewer"))
                    .findFirst()
                    .orElseThrow();
            assertEquals(Set.of("user:alice@example.com", "serviceAccount:svc@example.com"),
                    dto1.getMembers());
            assertEquals("Project", dto1.getScope());
            assertEquals("Roles/viewer access role", dto1.getDescription());

            // Second DTO assertions
            AccessPolicyDTO dto2 = dtos.stream()
                    .filter(d -> d.getRole().equals("roles/editor"))
                    .findFirst()
                    .orElseThrow();
            assertEquals(Set.of("group:devs@example.com"), dto2.getMembers());
            assertEquals("Project", dto2.getScope());
            assertEquals("Roles/editor access role", dto2.getDescription());

            // Verify the client was closed at end of try-with-resources
            verify(client).close();
        }
    }

    @Test
    void getAllPolicies_projectsClientCreateThrowsIOException_shouldWrapInRuntimeException() {
        // Mock ProjectsClient.create() to throw IOException
        try (MockedStatic<ProjectsClient> projectsClientMock = mockStatic(ProjectsClient.class)) {
            projectsClientMock.when(ProjectsClient::create)
                    .thenThrow(new IOException("transport failure"));

            AccessPolicyService service = new AccessPolicyService();

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.getAllPolicies("any-project"));

            assertTrue(ex.getMessage().contains("Failed to fetch IAM policies"));
            assertTrue(ex.getCause() instanceof IOException);
        }
    }
}

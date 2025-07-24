package com.reboot.zerotocloud.controller;

import com.reboot.zerotocloud.dto.AccessPolicyDTO;
import com.reboot.zerotocloud.service.AccessPolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccessPolicyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccessPolicyService accessPolicyService;

    private AccessPolicyController accessPolicyController;

    @BeforeEach
    void setUp() {
        accessPolicyController = new AccessPolicyController();

        // Inject the mock service and the projectId into the controller
        ReflectionTestUtils.setField(accessPolicyController, "accessPolicyService", accessPolicyService);
        ReflectionTestUtils.setField(accessPolicyController, "projectId", "test-project");

        mockMvc = MockMvcBuilders
                .standaloneSetup(accessPolicyController)
                .build();
    }

    @Test
    void getAllPolicies_returnsListOfPolicies() throws Exception {
        // Prepare test data
        AccessPolicyDTO dto1 = new AccessPolicyDTO();
        dto1.setRole("role1");
        dto1.setMembers(Set.of("memberA"));
        dto1.setScope("scopeA");
        dto1.setDescription("descriptionA");

        AccessPolicyDTO dto2 = new AccessPolicyDTO();
        dto2.setRole("role2");
        dto2.setMembers(Set.of("memberC"));
        dto2.setScope("scopeB");
        dto2.setDescription("descriptionB");

        List<AccessPolicyDTO> policies = List.of(dto1, dto2);

        // Mock service behavior
        when(accessPolicyService.getAllPolicies("test-project")).thenReturn(policies);

        // Execute GET request and verify response
        mockMvc.perform(get("/api/access-policies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].role").value("role1"))
                .andExpect(jsonPath("$[0].members[0]").value("memberA"))
                .andExpect(jsonPath("$[0].scope").value("scopeA"))
                .andExpect(jsonPath("$[0].description").value("descriptionA"))
                .andExpect(jsonPath("$[1].role").value("role2"))
                .andExpect(jsonPath("$[1].members[0]").value("memberC"))
                .andExpect(jsonPath("$[1].scope").value("scopeB"))
                .andExpect(jsonPath("$[1].description").value("descriptionB"));

        // Verify service invocation
        verify(accessPolicyService).getAllPolicies("test-project");
    }

    @Test
    void getAllPolicies_returnsEmptyList() throws Exception {
        // Mock service behavior to return empty list
        when(accessPolicyService.getAllPolicies("test-project"))
                .thenReturn(Collections.emptyList());

        // Execute GET request and verify empty JSON array
        mockMvc.perform(get("/api/access-policies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        // Verify service invocation
        verify(accessPolicyService).getAllPolicies("test-project");
    }
}

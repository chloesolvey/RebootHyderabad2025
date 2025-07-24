package com.reboot.zerotocloud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reboot.zerotocloud.dto.TeamRegistrationRequest;
import com.reboot.zerotocloud.service.GCPLabelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LabelControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GCPLabelService gcpLabelService;

    private LabelController labelController;

    @BeforeEach
    void setUp() {
        labelController = new LabelController();
        ReflectionTestUtils.setField(labelController, "gcpLabelService", gcpLabelService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(labelController)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void registerTeam_success() throws Exception {
        // Arrange
        TeamRegistrationRequest request = new TeamRegistrationRequest("proj1", "team", "dev");
        when(gcpLabelService.addTeamLabel("proj1", "dev")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/gcp/labels/register-team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Label registered successfully"));

        verify(gcpLabelService).addTeamLabel("proj1", "dev");
    }

    @Test
    void registerTeam_labelAlreadyExists() throws Exception {
        // Arrange
        TeamRegistrationRequest request = new TeamRegistrationRequest("proj1", "team", "dev");
        when(gcpLabelService.addTeamLabel("proj1", "dev")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/gcp/labels/register-team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Label already exists for project: proj1"));

        verify(gcpLabelService).addTeamLabel("proj1", "dev");
    }

    @Test
    void registerTeam_exceptionThrown() throws Exception {
        // Arrange
        TeamRegistrationRequest request = new TeamRegistrationRequest("proj1", "team", "dev");
        doThrow(new RuntimeException("GCP failure"))
                .when(gcpLabelService).addTeamLabel("proj1", "dev");

        // Act & Assert
        mockMvc.perform(post("/api/gcp/labels/register-team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to register label: GCP failure"));

        verify(gcpLabelService).addTeamLabel("proj1", "dev");
    }
}
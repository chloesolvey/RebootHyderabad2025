package com.lloyds.onboard.controller;

import com.lloyds.onboard.config.ErrorConfig;
import com.lloyds.onboard.entity.Application;
import com.lloyds.onboard.entity.ResumeApplication;
import com.lloyds.onboard.model.Constants;
import com.lloyds.onboard.service.ApplicationService;
import com.lloyds.onboard.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private Map<String, NotificationService> notificationServices;

    @MockBean
    private ErrorConfig errorConfig;

    @Test
    void shouldGetAllApplications() throws Exception {
        when(applicationService.getAllApplications()).thenReturn(List.of(new Application()));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetApplicationsByRmid() throws Exception {
        when(applicationService.getApplicationsByRmid("RM001")).thenReturn(List.of(new Application()));

        mockMvc.perform(get("/api/applications").param("rmid", "RM001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetApplicationById() throws Exception {
        Application app = new Application();
        app.setId(1L);
        when(applicationService.getApplication(1L)).thenReturn(app);

        mockMvc.perform(get("/api/applications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldCreateApplication() throws Exception {
        Application app = new Application();
        app.setFirstname("John");
        when(applicationService.createApplication(any())).thenReturn(app);

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    { "firstname": "John" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("John"));
    }


    @Test
    void shouldSubmitApplication() throws Exception {
        Application app = new Application();
        app.setAppid("APP123");
        when(applicationService.updateApplication(eq("APP123"), any())).thenReturn(app);

        mockMvc.perform(post("/api/applications/submit-application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    { "appid": "APP123" }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldResumeJourney() throws Exception {
        ResumeApplication resumeApp = new ResumeApplication();
        resumeApp.setMessage("OTP sent");
        when(applicationService.resumeJourney("token123")).thenReturn(resumeApp);

        mockMvc.perform(get("/api/applications/resume-journey").param("token", "token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent"));
    }

    @Test
    void shouldAssignRM() throws Exception {
        Application app = new Application();
        app.setRmid("RM101");
        when(applicationService.assignToRM(eq(1L), eq("RM101"))).thenReturn(app);

        mockMvc.perform(put("/api/applications/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    { "assignedTo": "RM101" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rmid").value("RM101"));
    }

    @Test
    void shouldUpdateApplicationByAppid() throws Exception {
        Application updated = new Application();
        updated.setAppid("APP456");
        when(applicationService.updateApplication(eq("APP456"), any())).thenReturn(updated);

        mockMvc.perform(put("/api/applications/APP456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    { "appid": "APP456" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appid").value("APP456"));
    }

    @Test
    void shouldDeleteApplicationById() throws Exception {
        doNothing().when(applicationService).deleteApplication(1L);

        mockMvc.perform(delete("/api/applications/1"))
                .andExpect(status().isOk());

        verify(applicationService).deleteApplication(1L);
    }
}
package com.lloyds.onboard.controller;

import com.lloyds.onboard.config.ErrorConfig;
import com.lloyds.onboard.entity.JourneyTemplate;
import com.lloyds.onboard.service.JourneyTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JourneyTemplateController.class)
class JourneyTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JourneyTemplateService journeyService;
    @MockBean
    private ErrorConfig errorConfig;

    @Test
    void shouldReturnAllJourneys() throws Exception {
        JourneyTemplate jt = new JourneyTemplate();
        jt.setId(1L);
        jt.setJourneytype("Personal");
        jt.setVersion("v1");

        when(journeyService.getAllJourneys()).thenReturn(List.of(jt));

        mockMvc.perform(get("/api/journeys"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].journeytype").value("Personal"))
                .andExpect(jsonPath("$[0].version").value("v1"));
    }

    @Test
    void shouldCreateNewJourney() throws Exception {
        JourneyTemplate jt = new JourneyTemplate();
        jt.setId(2L);
        jt.setJourneytype("Home Loan");
        jt.setVersion("v2");

        when(journeyService.createJourney(any())).thenReturn(jt);

        mockMvc.perform(post("/api/journeys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "journeytype": "Home Loan",
                      "version": "v2"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.journeytype").value("Home Loan"));
    }

    @Test
    void shouldUpdateJourneyIfExists() throws Exception {
        JourneyTemplate jt = new JourneyTemplate();
        jt.setId(3L);
        jt.setJourneytype("Updated");
        jt.setVersion("v3");

        when(journeyService.updateJourney(eq(3L), any())).thenReturn(Optional.of(jt));

        mockMvc.perform(put("/api/journeys/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "journeytype": "Updated",
                      "version": "v3"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.journeytype").value("Updated"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonexistentJourney() throws Exception {
        when(journeyService.updateJourney(eq(99L), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/journeys/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "journeytype": "Unknown",
                      "version": "v0"
                    }
                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteJourneySuccessfully() throws Exception {
        when(journeyService.deleteJourney(4L)).thenReturn(true);

        mockMvc.perform(delete("/api/journeys/4"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundOnDeleteFailure() throws Exception {
        when(journeyService.deleteJourney(404L)).thenReturn(false);

        mockMvc.perform(delete("/api/journeys/404"))
                .andExpect(status().isNotFound());
    }
}
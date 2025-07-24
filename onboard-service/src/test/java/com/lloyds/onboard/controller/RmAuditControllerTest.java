package com.lloyds.onboard.controller;

import com.lloyds.onboard.config.ErrorConfig;
import com.lloyds.onboard.entity.RmAudit;
import com.lloyds.onboard.service.RmAuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RmAuditController.class)
class RmAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RmAuditService service;
    @MockBean
    private ErrorConfig errorConfig;


    @Test
    void shouldLogAuditActionSuccessfully() throws Exception {
        RmAudit audit = new RmAudit();
        audit.setMessage("Status updated");

        when(service.createAudit(any())).thenReturn(audit);

        mockMvc.perform(post("/api/rmaudit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "message": "Status updated"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Status updated"));
    }

    @Test
    void shouldReturnAuditHistoryByApplicationId() throws Exception {
        RmAudit audit1 = new RmAudit();
        RmAudit audit2 = new RmAudit();
        List<RmAudit> history = List.of(audit1, audit2);

        when(service.getAuditHistory(101L)).thenReturn(history);

        mockMvc.perform(get("/api/rmaudit/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
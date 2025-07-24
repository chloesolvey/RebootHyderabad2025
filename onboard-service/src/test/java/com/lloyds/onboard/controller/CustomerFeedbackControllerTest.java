package com.lloyds.onboard.controller;

import com.lloyds.onboard.config.ErrorConfig;
import com.lloyds.onboard.entity.CustomerFeedback;
import com.lloyds.onboard.service.CustomerFeedbackService;
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

@WebMvcTest(CustomerFeedbackController.class)
class CustomerFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerFeedbackService service;
    @MockBean
    private ErrorConfig errorConfig;

    @Test
    void shouldSubmitCustomerFeedback() throws Exception {
        CustomerFeedback feedback = new CustomerFeedback();
        feedback.setFeedback("Great service!");

        when(service.saveFeedback(any())).thenReturn(feedback);

        mockMvc.perform(post("/api/customerfeedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "feedback": "Great service!"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feedback").value("Great service!"));
    }

    @Test
    void shouldFetchFeedbackByApplicationId() throws Exception {
        CustomerFeedback feedback1 = new CustomerFeedback();
        CustomerFeedback feedback2 = new CustomerFeedback();

        when(service.getFeedbackByApplicationId(101L))
                .thenReturn(List.of(feedback1, feedback2));

        mockMvc.perform(get("/api/customerfeedback/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
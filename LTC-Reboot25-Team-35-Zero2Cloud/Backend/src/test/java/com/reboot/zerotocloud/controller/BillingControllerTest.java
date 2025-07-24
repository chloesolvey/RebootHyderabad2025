package com.reboot.zerotocloud.controller;

import com.reboot.zerotocloud.model.BillingInfo;
import com.reboot.zerotocloud.service.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BillingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BillingService billingService;

    private BillingController billingController;

    @BeforeEach
    void setUp() {
        billingController = new BillingController();
        ReflectionTestUtils.setField(billingController, "billingService", billingService);
        mockMvc = MockMvcBuilders.standaloneSetup(billingController).build();
    }

    @Test
    void getBillingSummary_successfulResponse() throws Exception {
        // Arrange
        BillingInfo info = new BillingInfo();
        info.setCurrentMonthSpend(1234.56);
        info.setPreviousMonthSpend(789.01);
        info.setProjectedSpend(1500.00);
        info.setServiceWiseCosts(Map.of(
            "Compute Engine", 800.00,
            "Cloud Storage", 434.56
        ));

        when(billingService.fetchBillingData()).thenReturn(info);

        // Act & Assert
        mockMvc.perform(get("/api/gcp/billing/summary"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.currentMonthSpend").value(1234.56))
            .andExpect(jsonPath("$.previousMonthSpend").value(789.01))
            .andExpect(jsonPath("$.projectedSpend").value(1500.00));

        verify(billingService).fetchBillingData();
    }

    @Test
    void getBillingSummary_serviceThrowsException_returnsInternalServerError() throws Exception {
        // Arrange
        doThrow(new RuntimeException("GCP API failure"))
            .when(billingService).fetchBillingData();

        // Act & Assert
        mockMvc.perform(get("/api/gcp/billing/summary"))
            .andExpect(status().isInternalServerError());

        verify(billingService).fetchBillingData();
    }
}
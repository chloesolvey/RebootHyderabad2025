package com.lloyds.onboard.controller;

import com.lloyds.onboard.config.ErrorConfig;
import com.lloyds.onboard.model.OtpRequest;
import com.lloyds.onboard.service.OtpService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OtpController.class)
class OtpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OtpService otpService;

    @MockBean
    private ErrorConfig errorConfig;

    @Test
    void shouldGenerateOtpSuccessfully() throws Exception {
        OtpRequest request = new OtpRequest("sms", "9999999999");
        when(otpService.generateOtp(any())).thenReturn("OTP sent successfully via sms");

        mockMvc.perform(post("/api/otp/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "mode": "sms",
                      "recipient": "9999999999"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP sent successfully via sms"));
    }

    @Test
    void shouldGenerateOtpWithFailureResponse() throws Exception {
        when(otpService.generateOtp(any())).thenReturn("OTP failed to send");

        mockMvc.perform(post("/api/otp/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "mode": "sms",
                      "recipient": "9999999999"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("OTP failed to send"));
    }

    @Test
    void shouldVerifyOtpSuccessfully() throws Exception {
        when(otpService.verifyOtp(any())).thenReturn("OTP verified successfully.");

        mockMvc.perform(post("/api/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "recipient": "9999999999",
                      "otp": "123456"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP verified successfully."));
    }

    @Test
    void shouldReturnInvalidOtpResponse() throws Exception {
        when(otpService.verifyOtp(any())).thenReturn("Invalid OTP.");

        mockMvc.perform(post("/api/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "recipient": "9999999999",
                      "otp": "000000"
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid OTP."));
    }
}
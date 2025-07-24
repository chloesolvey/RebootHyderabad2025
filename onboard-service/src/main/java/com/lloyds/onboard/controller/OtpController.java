package com.lloyds.onboard.controller;

import com.lloyds.onboard.model.OtpRequest;
import com.lloyds.onboard.model.OtpVerifyRequest;
import com.lloyds.onboard.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller that handles OTP (One-Time Password) related requests.
 * Provides endpoints for generating and verifying OTPs.
 * <p>
 * Base URL mapping for this controller is "/api/otp".
 * </p>
 */
@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private final OtpService otpService;

    /**
     * Constructs an OtpController with the given OtpService.
     *
     * @param otpService the service responsible for OTP generation and verification
     */
    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    /**
     * Generates an OTP based on the supplied {@link OtpRequest}.
     * <p>
     * Accepts a POST request at "/generate" with an OtpRequest payload.
     * Returns a JSON response containing whether the generation was successful and a message.
     * </p>
     *
     * @param request the OTP generation request containing necessary details
     * @return a ResponseEntity with a map containing keys "success" (boolean) and "message" (String)
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateOtp(@RequestBody OtpRequest request) {
        String result = otpService.generateOtp(request);
        boolean success = result.contains("successfully");
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    /**
     * Verifies an OTP based on the supplied {@link OtpVerifyRequest}.
     * <p>
     * Accepts a POST request at "/verify" with an OtpVerifyRequest payload.
     * Returns a JSON response containing whether the verification was successful and a message.
     * </p>
     *
     * @param request the OTP verification request containing OTP and other necessary details
     * @return a ResponseEntity with a map containing keys "success" (boolean) and "message" (String)
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody OtpVerifyRequest request) {
        String result = otpService.verifyOtp(request);
        boolean success = result.equalsIgnoreCase("OTP verified successfully.");
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", result);
        return ResponseEntity.ok(response);
    }
}

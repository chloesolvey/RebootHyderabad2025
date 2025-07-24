package com.lloyds.onboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpVerifyRequest {
    private String recipient;
    private String otp;
    private Long id;

    // Getters and Setters
}
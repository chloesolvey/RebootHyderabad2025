package com.lloyds.onboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpRequest {
    private String recipient; // mobile or email
    private String mode;      // "sms" or "email"

    // Getters and Setters
}

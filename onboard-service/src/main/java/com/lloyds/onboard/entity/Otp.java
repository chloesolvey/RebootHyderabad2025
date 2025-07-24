package com.lloyds.onboard.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp")
@Data
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipient;
    private String otp;
    private String mode;

    private boolean used;
    private LocalDateTime createdate;

    // Getters and Setters
}

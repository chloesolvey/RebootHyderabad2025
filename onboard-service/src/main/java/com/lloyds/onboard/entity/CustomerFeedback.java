package com.lloyds.onboard.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "customerfeedback")
@Data
public class CustomerFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long applicationid;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    private LocalDateTime createddate = LocalDateTime.now();
}

package com.lloyds.onboard.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "rmaudit")
@Data
public class RmAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rmid;

    // No foreign key object reference, only plain ID
    private Long applicationid;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createddate = LocalDateTime.now();
}

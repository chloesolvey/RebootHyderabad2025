package com.lloyds.onboard.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "rmuser")
@Data
public class RmUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String rmid;

    private String name;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "createddate")
    private LocalDateTime createdAt;

    public enum Role {
        RM, ADMIN
    }

    // Getters and Setters
}

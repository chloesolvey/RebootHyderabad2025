package com.lloyds.onboard.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String appid;

    @Column(nullable = false)
    private String journeytype;

    @Enumerated(EnumType.STRING)
    private Status status = Status.inprogress;

    private String rmid;
    private String salutation;
    private String firstname;
    private String lastname;
    private String mobilenumber;
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String postalcode;

    @Column(columnDefinition = "LONGTEXT")
    private String formdata;

    private Integer currentpage;

    private LocalDateTime createddate = LocalDateTime.now();

    private LocalDateTime updateddate = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.createddate = LocalDateTime.now();
        this.appid = this.journeytype +"-"+ System.currentTimeMillis()/1000;
    }

    public enum Status {
        inprogress, submitted
    }
}

package com.lloyds.onboard.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "rmmapping")
public class RMMappings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rmid", nullable = false)
    private String rmid;

    @Column(name = "rmname")
    private String rmname;

    @Column(name = "pincode", nullable = false)
    private String pincode;

    @Column(name = "journeytype", nullable = false)
    private String journeytype;


}

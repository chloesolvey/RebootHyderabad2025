// src/main/java/com/example/demo/entity/JourneyTemplate.java
package com.lloyds.onboard.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "journeytemplate")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JourneyTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "journeytype", nullable = false)
    private String journeytype;

    @Column(name = "version", nullable = false)
    private String version = "v1";

    @Lob
    @Column(name = "templatedata", columnDefinition = "json", nullable = false)
    private String templatedata;

    @Column(name = "createddate", columnDefinition = "datetime")
    private LocalDateTime createddate = LocalDateTime.now();
}

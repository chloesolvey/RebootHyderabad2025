package com.lloyds.onboard.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "applicationdocuments")
public class ApplicationDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "applicationid", nullable = false)
    private Long applicationId;

    @Column(name = "pagenumber", nullable = false)
    private Integer pageNumber;

    @Column(name = "fieldname", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "filename", nullable = false, length = 255)
    private String fileName;

    @Column(name = "filetype", length = 100)
    private String fileType;

    @Lob
    @Column(name = "filecontent", nullable = false)
    private byte[] fileContent;

    @Column(name = "createddate", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

}
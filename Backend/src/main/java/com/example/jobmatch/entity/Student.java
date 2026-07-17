package com.example.jobmatch.entity;

import com.jobmatch.entity.Enums.WorkAuthStatus;
import com.jobmatch.entity.Enums.WorkMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "students")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private BigDecimal gpa;

    @Column(name = "grad_year")
    private Integer gradYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_auth_status", nullable = false)
    private WorkAuthStatus workAuthStatus;

    @Column(name = "needs_sponsorship", nullable = false)
    private boolean needsSponsorship;

    @Column(name = "resume_url")
    private String resumeUrl;

    @Column(name = "preferred_locations")
    private String preferredLocations;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_work_mode")
    private WorkMode preferredWorkMode;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}

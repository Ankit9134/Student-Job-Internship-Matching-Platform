package com.example.jobmatch.dto;

import com.example.jobmatch.entity.Enums.ApplicationStatus;
import lombok.*;

import java.time.Instant;

public class ApplicationDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateApplicationRequest {
        private Long listingId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateStatusRequest {
        private ApplicationStatus status;
        private String notes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApplicationResponse {
        private Long id;
        private Long listingId;
        private String listingTitle;
        private String companyName;
        private ApplicationStatus status;
        private Instant appliedAt;
        private Instant postedAt;
        private String notes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApplicantSummary {
        private Long applicationId;
        private Long studentId;
        private String studentName;
        private String email;
        private java.math.BigDecimal gpa;
        private Integer gradYear;
        private String workAuthStatus;
        private boolean needsSponsorship;
        private String preferredWorkMode;
        private String preferredLocations;
        private boolean hasResume;
        private java.util.List<String> skills;
        private ApplicationStatus status;
        private Instant appliedAt;
        private String notes;
    }
}

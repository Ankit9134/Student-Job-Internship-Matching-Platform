package com.example.jobmatch.dto;

import com.jobmatch.entity.Enums.ApplicationStatus;
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
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApplicationResponse {
        private Long id;
        private Long listingId;
        private String listingTitle;
        private String companyName;
        private ApplicationStatus status;
        private Instant appliedAt;
    }
}

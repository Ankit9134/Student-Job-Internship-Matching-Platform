package com.example.jobmatch.dto;

import com.example.jobmatch.entity.Enums.WorkAuthStatus;
import com.example.jobmatch.entity.Enums.WorkMode;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class StudentDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StudentProfileRequest {
        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String fullName;

        @DecimalMin("0.0") @DecimalMax("4.0")
        private BigDecimal gpa;

        private Integer gradYear;

        @NotNull
        private WorkAuthStatus workAuthStatus;

        private boolean needsSponsorship;

        private String preferredLocations;

        private WorkMode preferredWorkMode;

        /** List of skill IDs the student selected via the tag picker. */
        private List<Long> skillIds;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StudentProfileResponse {
        private Long id;
        private String email;
        private String fullName;
        private BigDecimal gpa;
        private Integer gradYear;
        private WorkAuthStatus workAuthStatus;
        private boolean needsSponsorship;
        private String resumeUrl;
        private String preferredLocations;
        private WorkMode preferredWorkMode;
        private List<SkillDto> skills;
    }
}

package com.example.jobmatch.dto;

import com.example.jobmatch.entity.Enums.RoleType;
import com.example.jobmatch.entity.Enums.WorkMode;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ListingDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ListingRequest {
        @NotBlank
        private String title;

        @NotBlank
        private String companyName;

        private String description;
        private String location;

        @NotNull
        private WorkMode workMode;

        private BigDecimal minGpa;
        private boolean sponsorshipOffered;
        private RoleType roleType;
        private Long createdBy;

        /** skillId -> weight (1 = nice-to-have, 2 = required) */
        private Map<Long, Integer> skillWeights;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ListingResponse {
        private Long id;
        private String title;
        private String companyName;
        private String description;
        private String location;
        private WorkMode workMode;
        private BigDecimal minGpa;
        private boolean sponsorshipOffered;
        private RoleType roleType;
        private List<SkillDto> requiredSkills;
        private List<SkillDto> niceToHaveSkills;
    }
}

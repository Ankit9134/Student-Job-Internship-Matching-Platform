package com.example.jobmatch.dto;

import com.example.jobmatch.entity.Enums.RoleType;
import com.example.jobmatch.entity.Enums.WorkMode;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class MatchDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MatchBreakdown {
        private BigDecimal skillScore;
        private BigDecimal gpaScore;
        private BigDecimal authScore;
        private List<String> matchedSkills;
        private List<String> missingSkills;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MatchCardResponse {
        private Long listingId;
        private String title;
        private String company;
        private String location;
        private WorkMode workMode;
        private RoleType roleType;
        private boolean sponsorshipOffered;
        private BigDecimal score;
        private MatchBreakdown breakdown;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PagedResponse<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    /** Recruiter-side: which students match a given listing. */
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StudentMatchResponse {
        private Long studentId;
        private String studentName;
        private BigDecimal score;
        private MatchBreakdown breakdown;
    }
}

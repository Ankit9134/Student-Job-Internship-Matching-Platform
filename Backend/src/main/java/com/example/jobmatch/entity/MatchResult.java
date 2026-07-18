package com.example.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "match_results", indexes = {
    @Index(name = "idx_mr_student_score", columnList = "student_id, score DESC"),
    @Index(name = "idx_mr_listing_score", columnList = "listing_id, score DESC")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(MatchResult.MatchResultId.class)
public class MatchResult {

    @Id
    @Column(name = "student_id")
    private Long studentId;

    @Id
    @Column(name = "listing_id")
    private Long listingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private Listing listing;

    @Column(nullable = false)
    private BigDecimal score;

    @Column(name = "skill_score")
    private BigDecimal skillScore;

    @Column(name = "gpa_score")
    private BigDecimal gpaScore;

    @Column(name = "auth_score")
    private BigDecimal authScore;

    @Column(name = "mode_score")
    private BigDecimal modeScore;

    @Column(name = "matched_skills", columnDefinition = "TEXT")
    private String matchedSkills;

    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills;

    @Column(name = "computed_at")
    private Instant computedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchResultId implements Serializable {
        private Long studentId;
        private Long listingId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MatchResultId)) return false;
            MatchResultId that = (MatchResultId) o;
            return Objects.equals(studentId, that.studentId) && Objects.equals(listingId, that.listingId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(studentId, listingId);
        }
    }
}

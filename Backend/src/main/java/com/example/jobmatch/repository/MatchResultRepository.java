package com.example.jobmatch.repository;

import com.example.jobmatch.entity.MatchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchResultRepository extends JpaRepository<MatchResult, MatchResult.MatchResultId> {

    /**
     * Core matches query: joins match_results -> listings, applies optional filters,
     * and relies on Pageable for sort (defaults to score DESC from the service layer).
     * Each filter param is treated as "no filter" when null/blank.
     */
    @Query("""
        SELECT mr FROM MatchResult mr
        JOIN Listing l ON l.id = mr.listingId
        WHERE mr.studentId = :studentId
          AND l.active = true
          AND (:role IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :role, '%')))
          AND (:location IS NULL OR LOWER(l.location) LIKE LOWER(CONCAT('%', :location, '%')))
          AND (:workMode IS NULL OR l.workMode = :workMode)
          AND (:sponsorshipNeeded IS NULL OR l.sponsorshipOffered = :sponsorshipNeeded)
        """)
    Page<MatchResult> findFilteredMatches(
        @Param("studentId") Long studentId,
        @Param("role") String role,
        @Param("location") String location,
        @Param("workMode") com.jobmatch.entity.Enums.WorkMode workMode,
        @Param("sponsorshipNeeded") Boolean sponsorshipNeeded,
        Pageable pageable
    );

    /** Recruiter-side view: top matching students for a given listing. */
    @Query("SELECT mr FROM MatchResult mr WHERE mr.listingId = :listingId")
    Page<MatchResult> findByListingId(@Param("listingId") Long listingId, Pageable pageable);
}

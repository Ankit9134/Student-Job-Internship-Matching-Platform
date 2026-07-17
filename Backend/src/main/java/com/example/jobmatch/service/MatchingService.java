package com.example.jobmatch.service;

import com.example.jobmatch.config.MatchingWeightsConfig;
import com.example.jobmatch.entity.*;
import com.example.jobmatch.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Computes weighted match scores between students and listings and persists them
 * into match_results. Recomputation is triggered explicitly (on profile save, on
 * listing create/update) rather than on every read - see recomputeForStudent /
 * recomputeForListing. The /matches endpoint then just reads this cache.
 */
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final StudentRepository studentRepo;
    private final ListingRepository listingRepo;
    private final MatchResultRepository matchRepo;
    private final StudentSkillRepository studentSkillRepo;
    private final ListingSkillRepository listingSkillRepo;
    private final SkillRepository skillRepo;
    private final MatchingWeightsConfig weights;

    @Transactional
    public void recomputeForStudent(Long studentId) {
        Student student = studentRepo.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        Set<Long> studentSkillIds = studentSkillRepo.findSkillIdsByStudent(studentId);
        Map<Long, String> skillNames = loadSkillNames();

        List<Listing> activeListings = listingRepo.findAllByActiveTrue();
        List<MatchResult> results = activeListings.stream()
            .map(listing -> score(student, listing, studentSkillIds, skillNames))
            .collect(Collectors.toList());

        matchRepo.saveAll(results);
    }

    @Transactional
    public void recomputeForListing(Long listingId) {
        Listing listing = listingRepo.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found: " + listingId));
        Map<Long, String> skillNames = loadSkillNames();

        List<Student> students = studentRepo.findAll();
        List<MatchResult> results = students.stream()
            .map(student -> score(student, listing, studentSkillRepo.findSkillIdsByStudent(student.getId()), skillNames))
            .collect(Collectors.toList());

        matchRepo.saveAll(results);
    }

    /** Recomputes the entire matrix. Intended for a scheduled/nightly safety-net job. */
    @Transactional
    public void recomputeAll() {
        List<Student> students = studentRepo.findAll();
        List<Listing> listings = listingRepo.findAllByActiveTrue();
        Map<Long, String> skillNames = loadSkillNames();

        for (Student student : students) {
            Set<Long> studentSkillIds = studentSkillRepo.findSkillIdsByStudent(student.getId());
            List<MatchResult> results = listings.stream()
                .map(listing -> score(student, listing, studentSkillIds, skillNames))
                .collect(Collectors.toList());
            matchRepo.saveAll(results);
        }
    }

    private Map<Long, String> loadSkillNames() {
        return skillRepo.findAll().stream().collect(Collectors.toMap(Skill::getId, Skill::getName));
    }

    private MatchResult score(Student student, Listing listing, Set<Long> studentSkillIds, Map<Long, String> skillNames) {
        List<ListingSkill> listingSkills = listingSkillRepo.findByListingId(listing.getId());

        // ---- Skill score: weighted overlap ----
        int totalWeight = listingSkills.stream().mapToInt(ls -> nz(ls.getWeight())).sum();
        int matchedWeight = listingSkills.stream()
            .filter(ls -> studentSkillIds.contains(ls.getSkillId()))
            .mapToInt(ls -> nz(ls.getWeight()))
            .sum();
        double skillScore = totalWeight == 0 ? 100.0 : 100.0 * matchedWeight / totalWeight;

        List<String> matchedNames = listingSkills.stream()
            .map(ListingSkill::getSkillId)
            .filter(studentSkillIds::contains)
            .map(id -> skillNames.getOrDefault(id, "Unknown"))
            .collect(Collectors.toList());
        List<String> missingNames = listingSkills.stream()
            .map(ListingSkill::getSkillId)
            .filter(id -> !studentSkillIds.contains(id))
            .map(id -> skillNames.getOrDefault(id, "Unknown"))
            .collect(Collectors.toList());

        // ---- GPA score: hard pass at threshold, linear taper below it ----
        double gpaScore;
        BigDecimal minGpa = listing.getMinGpa();
        BigDecimal studentGpa = student.getGpa();
        if (minGpa == null || minGpa.compareTo(BigDecimal.ZERO) == 0) {
            gpaScore = 100.0;
        } else if (studentGpa != null && studentGpa.compareTo(minGpa) >= 0) {
            gpaScore = 100.0;
        } else {
            double gap = minGpa.doubleValue() - (studentGpa == null ? 0 : studentGpa.doubleValue());
            gpaScore = Math.max(0, 100.0 * (1 - gap));
        }

        // ---- Work-auth score: binary compatibility check ----
        double authScore = (listing.isSponsorshipOffered() || !student.isNeedsSponsorship()) ? 100.0 : 0.0;

        double finalScore = skillScore * weights.getWeightSkill()
            + gpaScore * weights.getWeightGpa()
            + authScore * weights.getWeightAuth();

        return MatchResult.builder()
            .studentId(student.getId())
            .listingId(listing.getId())
            .score(round(finalScore))
            .skillScore(round(skillScore))
            .gpaScore(round(gpaScore))
            .authScore(round(authScore))
            .matchedSkills(String.join(",", matchedNames))
            .missingSkills(String.join(",", missingNames))
            .computedAt(Instant.now())
            .build();
    }

    private int nz(Integer i) {
        return i == null ? 0 : i;
    }

    private BigDecimal round(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }
}

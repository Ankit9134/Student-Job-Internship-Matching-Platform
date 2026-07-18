package com.example.jobmatch.service;

import com.example.jobmatch.config.MatchingWeightsConfig;
import com.example.jobmatch.entity.*;
import com.example.jobmatch.entity.Enums;
import com.example.jobmatch.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
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

    @Async
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void recomputeForStudent(Long studentId) {
        Student student = studentRepo.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        Set<Long> studentSkillIds = studentSkillRepo.findSkillIdsByStudent(studentId);
        Map<Long, String> skillNames = loadSkillNames();
        Map<Long, List<ListingSkill>> listingSkillsMap = loadListingSkillsMap();

        List<Listing> activeListings = listingRepo.findAllByActiveTrue();
        List<MatchResult> results = activeListings.stream()
            .map(listing -> score(student, listing, studentSkillIds, skillNames, listingSkillsMap.getOrDefault(listing.getId(), List.of())))
            .collect(Collectors.toList());

        matchRepo.saveAll(results);
    }

    @Async
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void recomputeForListing(Long listingId) {
        Listing listing = listingRepo.findById(listingId)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found: " + listingId));
        Map<Long, String> skillNames = loadSkillNames();
        List<ListingSkill> listingSkills = listingSkillRepo.findByListingId(listingId);

        List<Student> students = studentRepo.findAll();
        Set<Long> studentIds = students.stream().map(Student::getId).collect(Collectors.toSet());

        Map<Long, Set<Long>> allStudentSkills = studentSkillRepo.findByStudentIdIn(studentIds).stream()
            .collect(Collectors.groupingBy(
                com.example.jobmatch.entity.StudentSkill::getStudentId,
                Collectors.mapping(com.example.jobmatch.entity.StudentSkill::getSkillId, Collectors.toSet())
            ));
        List<MatchResult> results = students.stream()
            .map(student -> score(student, listing,
                allStudentSkills.getOrDefault(student.getId(), Set.of()),
                skillNames, listingSkills))
            .collect(Collectors.toList());

        matchRepo.saveAll(results);
    }

    @Transactional
    public void recomputeAll() {
        List<Student> students = studentRepo.findAll();
        List<Listing> listings = listingRepo.findAllByActiveTrue();
        Map<Long, String> skillNames = loadSkillNames();
        Map<Long, List<ListingSkill>> listingSkillsMap = loadListingSkillsMap();

        Map<Long, Set<Long>> allStudentSkills = studentSkillRepo.findAll().stream()
            .collect(Collectors.groupingBy(
                com.example.jobmatch.entity.StudentSkill::getStudentId,
                Collectors.mapping(com.example.jobmatch.entity.StudentSkill::getSkillId, Collectors.toSet())
            ));

        for (Student student : students) {
            Set<Long> studentSkillIds = allStudentSkills.getOrDefault(student.getId(), Set.of());
            List<MatchResult> results = listings.stream()
                .map(listing -> score(student, listing, studentSkillIds, skillNames, listingSkillsMap.getOrDefault(listing.getId(), List.of())))
                .collect(Collectors.toList());
            matchRepo.saveAll(results);
        }
    }

    private Map<Long, List<ListingSkill>> loadListingSkillsMap() {
        return listingSkillRepo.findAll().stream()
            .collect(Collectors.groupingBy(ListingSkill::getListingId));
    }

    private Map<Long, String> loadSkillNames() {
        return skillRepo.findAll().stream().collect(Collectors.toMap(Skill::getId, Skill::getName));
    }

    private MatchResult score(Student student, Listing listing, Set<Long> studentSkillIds, Map<Long, String> skillNames, List<ListingSkill> listingSkills) {

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

        // ---- GPA score: linear taper below threshold, normalized over max GPA (10) ----
        double gpaScore;
        BigDecimal minGpa = listing.getMinGpa();
        BigDecimal studentGpa = student.getGpa();
        if (minGpa == null || minGpa.compareTo(BigDecimal.ZERO) == 0) {
            gpaScore = 100.0;
        } else if (studentGpa != null && studentGpa.compareTo(minGpa) >= 0) {
            gpaScore = 100.0;
        } else {
            double sGpa = studentGpa == null ? 0.0 : studentGpa.doubleValue();
            double mGpa = minGpa.doubleValue();
            // linear: 0% at GPA=0, 100% at GPA=minGpa
            gpaScore = Math.max(0.0, 100.0 * sGpa / mGpa);
        }

        // ---- Work-auth score: tiered by visa/sponsorship compatibility ----
        double authScore;
        boolean sponsorshipNeeded = student.isNeedsSponsorship();
        boolean sponsorshipOffered = listing.isSponsorshipOffered();
        Enums.WorkAuthStatus authStatus = student.getWorkAuthStatus();
        if (authStatus == Enums.WorkAuthStatus.CITIZEN || authStatus == Enums.WorkAuthStatus.PERM_RESIDENT) {
            authScore = 100.0; // never needs sponsorship
        } else if (!sponsorshipNeeded) {
            authScore = 90.0;  // on OPT/other but self-sufficient
        } else if (sponsorshipOffered) {
            authScore = 80.0;  // needs sponsorship and listing offers it
        } else {
            authScore = 0.0;   // needs sponsorship but listing doesn't offer it
        }

        // ---- Work-mode score: preference alignment ----
        double modeScore;
        Enums.WorkMode preferred = student.getPreferredWorkMode();
        Enums.WorkMode offered   = listing.getWorkMode();
        if (preferred == null || preferred == Enums.WorkMode.ANY) {
            modeScore = 100.0;
        } else if (preferred == offered) {
            modeScore = 100.0;
        } else if (offered == Enums.WorkMode.HYBRID) {
            modeScore = 60.0;  // hybrid is a partial match for remote/onsite preference
        } else {
            modeScore = 20.0;  // mismatch
        }

        double finalScore = skillScore * weights.getWeightSkill()
            + gpaScore  * weights.getWeightGpa()
            + authScore * weights.getWeightAuth()
            + modeScore * weights.getWeightMode();

        return MatchResult.builder()
            .studentId(student.getId())
            .listingId(listing.getId())
            .score(round(finalScore))
            .skillScore(round(skillScore))
            .gpaScore(round(gpaScore))
            .authScore(round(authScore))
            .modeScore(round(modeScore))
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

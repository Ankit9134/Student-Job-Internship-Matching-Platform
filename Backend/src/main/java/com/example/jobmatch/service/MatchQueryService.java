package com.example.jobmatch.service;

import com.example.jobmatch.dto.MatchDtos.MatchBreakdown;
import com.example.jobmatch.dto.MatchDtos.MatchCardResponse;
import com.example.jobmatch.dto.MatchDtos.PagedResponse;
import com.example.jobmatch.dto.MatchDtos.StudentMatchResponse;
import com.example.jobmatch.entity.Enums.WorkMode;
import com.example.jobmatch.entity.Listing;
import com.example.jobmatch.entity.MatchResult;
import com.example.jobmatch.entity.Student;
import com.example.jobmatch.repository.ListingRepository;
import com.example.jobmatch.repository.MatchResultRepository;
import com.example.jobmatch.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchQueryService {

    private final MatchResultRepository matchRepo;
    private final ListingRepository listingRepo;
    private final StudentRepository studentRepo;

    /** Student-facing: ranked, filtered matches. Defaults to sorting by score descending. */
    @Transactional(readOnly = true)
    public PagedResponse<MatchCardResponse> getMatchesForStudent(
        Long studentId, String role, String location, WorkMode workMode, Boolean sponsorshipNeeded,
        int page, int size, String sort
    ) {
        Sort sortSpec = "score".equalsIgnoreCase(sort)
            ? Sort.by(Sort.Direction.DESC, "score")
            : Sort.by(Sort.Direction.DESC, "computedAt");
        Pageable pageable = PageRequest.of(page, size, sortSpec);

        Page<MatchResult> results = matchRepo.findFilteredMatches(
            studentId, role, location, workMode, sponsorshipNeeded, pageable
        );

        List<Long> listingIds = results.getContent().stream().map(MatchResult::getListingId).collect(Collectors.toList());
        Map<Long, Listing> listingMap = listingRepo.findAllById(listingIds).stream()
            .collect(Collectors.toMap(Listing::getId, l -> l));

        List<MatchCardResponse> content = results.getContent().stream()
            .map(mr -> toCard(mr, listingMap.get(mr.getListingId())))
            .collect(Collectors.toList());

        return PagedResponse.<MatchCardResponse>builder()
            .content(content)
            .page(results.getNumber())
            .size(results.getSize())
            .totalElements(results.getTotalElements())
            .totalPages(results.getTotalPages())
            .build();
    }

    /** Full breakdown for the explain-match tooltip / detail view. */
    @Transactional(readOnly = true)
    public MatchCardResponse getExplain(Long studentId, Long listingId) {
        MatchResult mr = matchRepo.findById(new MatchResult.MatchResultId(studentId, listingId))
            .orElseThrow(() -> new com.example.jobmatch.exception.ResourceNotFoundException(
                "No computed match for student " + studentId + " / listing " + listingId));
        Listing listing = listingRepo.findById(listingId)
            .orElseThrow(() -> new com.example.jobmatch.exception.ResourceNotFoundException("Listing not found: " + listingId));
        return toCard(mr, listing);
    }

    /** Recruiter-facing: top students for a given listing, ranked by score. */
    @Transactional(readOnly = true)
    public PagedResponse<StudentMatchResponse> getMatchesForListing(Long listingId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "score"));
        Page<MatchResult> results = matchRepo.findByListingId(listingId, pageable);

        List<Long> studentIds = results.getContent().stream().map(MatchResult::getStudentId).collect(Collectors.toList());
        Map<Long, Student> studentMap = studentRepo.findAllById(studentIds).stream()
            .collect(Collectors.toMap(Student::getId, s -> s));

        List<StudentMatchResponse> content = results.getContent().stream()
            .map(mr -> StudentMatchResponse.builder()
                .studentId(mr.getStudentId())
                .studentName(studentMap.containsKey(mr.getStudentId()) ? studentMap.get(mr.getStudentId()).getFullName() : "Unknown")
                .score(mr.getScore())
                .breakdown(toBreakdown(mr))
                .build())
            .collect(Collectors.toList());

        return PagedResponse.<StudentMatchResponse>builder()
            .content(content)
            .page(results.getNumber())
            .size(results.getSize())
            .totalElements(results.getTotalElements())
            .totalPages(results.getTotalPages())
            .build();
    }

    private MatchCardResponse toCard(MatchResult mr, Listing listing) {
        if (listing == null) return null;
        return MatchCardResponse.builder()
            .listingId(listing.getId())
            .title(listing.getTitle())
            .company(listing.getCompanyName())
            .location(listing.getLocation())
            .workMode(listing.getWorkMode())
            .roleType(listing.getRoleType())
            .sponsorshipOffered(listing.isSponsorshipOffered())
            .score(mr.getScore())
            .breakdown(toBreakdown(mr))
            .build();
    }

    private MatchBreakdown toBreakdown(MatchResult mr) {
        return MatchBreakdown.builder()
            .skillScore(mr.getSkillScore())
            .gpaScore(mr.getGpaScore())
            .authScore(mr.getAuthScore())
            .matchedSkills(splitCsv(mr.getMatchedSkills()))
            .missingSkills(splitCsv(mr.getMissingSkills()))
            .build();
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }
}

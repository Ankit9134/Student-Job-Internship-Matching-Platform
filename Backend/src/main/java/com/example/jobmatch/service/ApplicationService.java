package com.example.jobmatch.service;

import com.example.jobmatch.dto.ApplicationDtos.ApplicationResponse;
import com.example.jobmatch.dto.ApplicationDtos.ApplicantSummary;
import com.example.jobmatch.entity.Application;
import com.example.jobmatch.entity.Enums.ApplicationStatus;
import com.example.jobmatch.entity.Listing;
import com.example.jobmatch.entity.Student;
import com.example.jobmatch.exception.ResourceNotFoundException;
import com.example.jobmatch.repository.ApplicationRepository;
import com.example.jobmatch.repository.ListingRepository;
import com.example.jobmatch.repository.SkillRepository;
import com.example.jobmatch.repository.StudentRepository;
import com.example.jobmatch.repository.StudentSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepo;
    private final ListingRepository listingRepo;
    private final StudentRepository studentRepo;
    private final StudentSkillRepository studentSkillRepo;
    private final SkillRepository skillRepo;

    @Transactional
    public ApplicationResponse markApplied(Long studentId, Long listingId) {
        return applicationRepo.findByStudentIdAndListingId(studentId, listingId)
            .map(existing -> {
                Listing listing = listingRepo.findById(listingId).orElse(null);
                return toResponse(existing, listing);
            })
            .orElseGet(() -> {
                Listing listing = listingRepo.findById(listingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));
                Application application = Application.builder()
                    .studentId(studentId)
                    .listingId(listingId)
                    .status(ApplicationStatus.APPLIED)
                    .appliedAt(Instant.now())
                    .build();
                return toResponse(applicationRepo.save(application), listing);
            });
    }

    @Transactional
    public ApplicationResponse updateStatus(Long applicationId, ApplicationStatus status, String notes) {
        Application application = applicationRepo.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + applicationId));
        application.setStatus(status);
        if (notes != null) application.setNotes(notes);
        Application saved = applicationRepo.save(application);
        Listing listing = listingRepo.findById(saved.getListingId()).orElse(null);
        return toResponse(saved, listing);
    }

    @Transactional
    public void delete(Long applicationId) {
        if (!applicationRepo.existsById(applicationId))
            throw new ResourceNotFoundException("Application not found: " + applicationId);
        applicationRepo.deleteById(applicationId);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getForStudent(Long studentId) {
        return applicationRepo.findByStudentIdWithListing(studentId)
            .stream()
            .map(a -> toResponse(a, a.getListing()))
            .collect(Collectors.toList());
    }

    private ApplicationResponse toResponse(Application a, Listing listing) {
        return ApplicationResponse.builder()
            .id(a.getId())
            .listingId(a.getListingId())
            .listingTitle(listing != null ? listing.getTitle() : "Unknown")
            .companyName(listing != null ? listing.getCompanyName() : "Unknown")
            .status(a.getStatus())
            .appliedAt(a.getAppliedAt())
            .postedAt(listing != null ? listing.getCreatedAt() : null)
            .notes(a.getNotes())
            .build();
    }

    @Transactional(readOnly = true)
    public List<ApplicantSummary> getForListing(Long listingId) {
        List<Application> apps = applicationRepo.findByListingId(listingId);
        List<Long> studentIds = apps.stream().map(Application::getStudentId).collect(Collectors.toList());
        Map<Long, Student> studentMap = studentRepo.findAllById(studentIds)
            .stream().collect(Collectors.toMap(Student::getId, s -> s));

        // Load all skills for these students in one query
        Set<Long> studentIdSet = new java.util.HashSet<>(studentIds);
        Map<Long, List<String>> skillsByStudent = studentSkillRepo.findByStudentIdIn(studentIdSet)
            .stream()
            .collect(Collectors.groupingBy(
                com.example.jobmatch.entity.StudentSkill::getStudentId,
                Collectors.mapping(ss -> ss.getSkillId().toString(), Collectors.toList())
            ));
        // Resolve skill IDs to names
        Set<Long> allSkillIds = studentSkillRepo.findByStudentIdIn(studentIdSet)
            .stream().map(com.example.jobmatch.entity.StudentSkill::getSkillId).collect(Collectors.toSet());
        Map<Long, String> skillNameMap = skillRepo.findAllById(allSkillIds)
            .stream().collect(Collectors.toMap(com.example.jobmatch.entity.Skill::getId, com.example.jobmatch.entity.Skill::getName));
        Map<Long, List<String>> skillNamesByStudent = studentSkillRepo.findByStudentIdIn(studentIdSet)
            .stream()
            .collect(Collectors.groupingBy(
                com.example.jobmatch.entity.StudentSkill::getStudentId,
                Collectors.mapping(ss -> skillNameMap.getOrDefault(ss.getSkillId(), ""), Collectors.toList())
            ));

        return apps.stream().map(a -> {
            Student s = studentMap.get(a.getStudentId());
            return ApplicantSummary.builder()
                .applicationId(a.getId())
                .studentId(a.getStudentId())
                .studentName(s != null ? s.getFullName() : "Unknown")
                .email(s != null ? s.getEmail() : null)
                .gpa(s != null ? s.getGpa() : null)
                .gradYear(s != null ? s.getGradYear() : null)
                .workAuthStatus(s != null && s.getWorkAuthStatus() != null ? s.getWorkAuthStatus().name() : null)
                .needsSponsorship(s != null && s.isNeedsSponsorship())
                .preferredWorkMode(s != null && s.getPreferredWorkMode() != null ? s.getPreferredWorkMode().name() : null)
                .preferredLocations(s != null ? s.getPreferredLocations() : null)
                .hasResume(s != null && s.getResumeUrl() != null)
                .skills(skillNamesByStudent.getOrDefault(a.getStudentId(), List.of()))
                .status(a.getStatus())
                .appliedAt(a.getAppliedAt())
                .notes(a.getNotes())
                .build();
        }).collect(Collectors.toList());
    }
}

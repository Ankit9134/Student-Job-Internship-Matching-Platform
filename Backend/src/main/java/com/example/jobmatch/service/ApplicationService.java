package com.example.jobmatch.service;

import com.example.jobmatch.dto.ApplicationDtos.ApplicationResponse;
import com.example.jobmatch.entity.Application;
import com.example.jobmatch.entity.Enums.ApplicationStatus;
import com.example.jobmatch.entity.Listing;
import com.example.jobmatch.exception.ResourceNotFoundException;
import com.example.jobmatch.repository.ApplicationRepository;
import com.example.jobmatch.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepo;
    private final ListingRepository listingRepo;

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
            .notes(a.getNotes())
            .build();
    }
}

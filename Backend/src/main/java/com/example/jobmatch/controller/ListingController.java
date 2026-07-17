package com.example.jobmatch.controller;

import com.example.jobmatch.dto.ListingDtos.ListingRequest;
import com.example.jobmatch.dto.ListingDtos.ListingResponse;
import com.example.jobmatch.dto.MatchDtos.PagedResponse;
import com.example.jobmatch.dto.MatchDtos.StudentMatchResponse;
import com.example.jobmatch.entity.Listing;
import com.example.jobmatch.service.ListingService;
import com.example.jobmatch.service.MatchQueryService;
import com.example.jobmatch.service.MatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;
    private final MatchingService matchingService;
    private final MatchQueryService matchQueryService;

    @PostMapping
    public ListingResponse create(@Valid @RequestBody ListingRequest req) {
        Listing saved = listingService.create(req);
        matchingService.recomputeForListing(saved.getId()); // score this new listing against every student
        return listingService.toResponse(saved);
    }

    @PutMapping("/{id}")
    public ListingResponse update(@PathVariable Long id, @Valid @RequestBody ListingRequest req) {
        Listing saved = listingService.update(id, req);
        matchingService.recomputeForListing(id);
        return listingService.toResponse(saved);
    }

    @GetMapping
    public List<ListingResponse> browse(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<Listing> listings = listingService.browse(PageRequest.of(page, size));
        return listings.getContent().stream().map(listingService::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ListingResponse get(@PathVariable Long id) {
        return listingService.toResponse(listingService.getById(id));
    }

    /** Recruiter-side (stretch goal): top matching students for this listing. */
    @GetMapping("/{id}/matches")
    public PagedResponse<StudentMatchResponse> getMatchingStudents(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return matchQueryService.getMatchesForListing(id, page, size);
    }
}

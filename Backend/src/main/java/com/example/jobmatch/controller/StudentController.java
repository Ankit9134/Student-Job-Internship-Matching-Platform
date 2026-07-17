package com.example.jobmatch.controller;

import com.example.jobmatch.dto.MatchDtos.MatchCardResponse;
import com.example.jobmatch.dto.MatchDtos.PagedResponse;
import com.example.jobmatch.dto.StudentDtos.StudentProfileRequest;
import com.example.jobmatch.dto.StudentDtos.StudentProfileResponse;
import com.example.jobmatch.entity.Enums.WorkMode;
import com.example.jobmatch.service.FileStorageService;
import com.example.jobmatch.service.MatchQueryService;
import com.example.jobmatch.service.MatchingService;
import com.example.jobmatch.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final MatchingService matchingService;
    private final MatchQueryService matchQueryService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public StudentProfileResponse create(@Valid @RequestBody StudentProfileRequest req) {
        StudentProfileResponse created = studentService.createOrUpdate(null, req);
        matchingService.recomputeForStudent(created.getId()); // populate match_results immediately
        return created;
    }

    @GetMapping("/{id}")
    public StudentProfileResponse get(@PathVariable Long id) {
        return studentService.getById(id);
    }

    @PutMapping("/{id}/profile")
    public StudentProfileResponse updateProfile(@PathVariable Long id, @Valid @RequestBody StudentProfileRequest req) {
        StudentProfileResponse updated = studentService.createOrUpdate(id, req);
        matchingService.recomputeForStudent(id); // recompute is the whole point - no manual trigger needed
        return updated;
    }

    @PostMapping("/{id}/resume")
    public StudentProfileResponse uploadResume(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String storedPath = fileStorageService.storeResume(id, file);
        studentService.updateResumeUrl(id, storedPath);
        return studentService.getById(id);
    }

    /** The core ranked/filtered/explainable match list for a student. */
    @GetMapping("/{id}/matches")
    public PagedResponse<MatchCardResponse> getMatches(
        @PathVariable Long id,
        @RequestParam(required = false) String role,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) WorkMode workMode,
        @RequestParam(required = false) Boolean sponsorshipNeeded,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "score") String sort
    ) {
        return matchQueryService.getMatchesForStudent(id, role, location, workMode, sponsorshipNeeded, page, size, sort);
    }

    @GetMapping("/{id}/matches/{listingId}/explain")
    public MatchCardResponse explainMatch(@PathVariable Long id, @PathVariable Long listingId) {
        return matchQueryService.getExplain(id, listingId);
    }
}

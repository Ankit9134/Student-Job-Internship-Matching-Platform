package com.example.jobmatch.controller;

import com.jobmatch.dto.ApplicationDtos.ApplicationResponse;
import com.jobmatch.dto.ApplicationDtos.CreateApplicationRequest;
import com.jobmatch.dto.ApplicationDtos.UpdateStatusRequest;
import com.jobmatch.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/api/students/{studentId}/applications")
    public ApplicationResponse markApplied(@PathVariable Long studentId, @RequestBody CreateApplicationRequest req) {
        return applicationService.markApplied(studentId, req.getListingId());
    }

    @GetMapping("/api/students/{studentId}/applications")
    public List<ApplicationResponse> listForStudent(@PathVariable Long studentId) {
        return applicationService.getForStudent(studentId);
    }

    @PatchMapping("/api/applications/{applicationId}")
    public ApplicationResponse updateStatus(@PathVariable Long applicationId, @RequestBody UpdateStatusRequest req) {
        return applicationService.updateStatus(applicationId, req.getStatus());
    }
}

package com.example.jobmatch.controller;

import com.example.jobmatch.dto.ApplicationDtos.ApplicationResponse;
import com.example.jobmatch.dto.ApplicationDtos.CreateApplicationRequest;
import com.example.jobmatch.dto.ApplicationDtos.UpdateStatusRequest;
import com.example.jobmatch.service.ApplicationService;
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
        return applicationService.updateStatus(applicationId, req.getStatus(), req.getNotes());
    }

    @DeleteMapping("/api/applications/{applicationId}")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long applicationId) {
        applicationService.delete(applicationId);
    }
}

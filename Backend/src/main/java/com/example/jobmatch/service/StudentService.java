package com.example.jobmatch.service;

import com.example.jobmatch.dto.SkillDto;
import com.example.jobmatch.dto.StudentDtos.StudentProfileRequest;
import com.example.jobmatch.dto.StudentDtos.StudentProfileResponse;
import com.example.jobmatch.entity.Skill;
import com.example.jobmatch.entity.Student;
import com.example.jobmatch.entity.StudentSkill;
import com.example.jobmatch.exception.ResourceNotFoundException;
import com.example.jobmatch.repository.SkillRepository;
import com.example.jobmatch.repository.StudentRepository;
import com.example.jobmatch.repository.StudentSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepo;
    private final StudentSkillRepository studentSkillRepo;
    private final SkillRepository skillRepo;

    @Transactional
    public StudentProfileResponse createOrUpdate(Long existingId, StudentProfileRequest req) {
        Student student = existingId != null
            ? studentRepo.findById(existingId).orElseThrow(() -> new ResourceNotFoundException("Student not found: " + existingId))
            : new Student();

        student.setEmail(req.getEmail());
        student.setFullName(req.getFullName());
        student.setGpa(req.getGpa());
        student.setGradYear(req.getGradYear());
        student.setWorkAuthStatus(req.getWorkAuthStatus());
        student.setNeedsSponsorship(req.isNeedsSponsorship());
        student.setPreferredLocations(req.getPreferredLocations());
        student.setPreferredWorkMode(req.getPreferredWorkMode());
        student.setUpdatedAt(Instant.now());
        if (student.getCreatedAt() == null) {
            student.setCreatedAt(Instant.now());
        }

        Student saved = studentRepo.save(student);

        // Replace skill set wholesale - simplest correct approach for a profile edit form.
        studentSkillRepo.deleteByStudentId(saved.getId());
        if (req.getSkillIds() != null && !req.getSkillIds().isEmpty()) {
            List<StudentSkill> skills = req.getSkillIds().stream()
                .map(skillId -> StudentSkill.builder()
                    .studentId(saved.getId())
                    .skillId(skillId)
                    .proficiency(3)
                    .build())
                .collect(Collectors.toList());
            studentSkillRepo.saveAll(skills);
        }

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getById(Long id) {
        Student student = studentRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        return toResponse(student);
    }

    @Transactional
    public void updateResumeUrl(Long studentId, String resumeUrl) {
        Student student = studentRepo.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        student.setResumeUrl(resumeUrl);
        studentRepo.save(student);
    }

    private StudentProfileResponse toResponse(Student student) {
        List<StudentSkill> studentSkills = studentSkillRepo.findByStudentId(student.getId());
        Map<Long, Skill> skillMap = skillRepo.findAllById(
            studentSkills.stream().map(StudentSkill::getSkillId).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(Skill::getId, s -> s));

        List<SkillDto> skillDtos = studentSkills.stream()
            .map(StudentSkill::getSkillId)
            .map(skillMap::get)
            .filter(java.util.Objects::nonNull)
            .map(s -> SkillDto.builder().id(s.getId()).name(s.getName()).category(s.getCategory()).build())
            .collect(Collectors.toList());

        return StudentProfileResponse.builder()
            .id(student.getId())
            .email(student.getEmail())
            .fullName(student.getFullName())
            .gpa(student.getGpa())
            .gradYear(student.getGradYear())
            .workAuthStatus(student.getWorkAuthStatus())
            .needsSponsorship(student.isNeedsSponsorship())
            .resumeUrl(student.getResumeUrl())
            .preferredLocations(student.getPreferredLocations())
            .preferredWorkMode(student.getPreferredWorkMode())
            .skills(skillDtos)
            .build();
    }
}

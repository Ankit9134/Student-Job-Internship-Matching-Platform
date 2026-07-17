package com.example.jobmatch.controller;

import com.example.jobmatch.dto.SkillDto;
import com.example.jobmatch.entity.Skill;
import com.example.jobmatch.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillRepository skillRepo;

    @GetMapping
    public List<SkillDto> list() {
        return skillRepo.findAll().stream()
            .map(s -> SkillDto.builder().id(s.getId()).name(s.getName()).category(s.getCategory()).build())
            .collect(Collectors.toList());
    }

    @PostMapping
    public SkillDto create(@RequestBody SkillDto dto) {
        Skill saved = skillRepo.save(Skill.builder().name(dto.getName()).category(dto.getCategory()).build());
        return SkillDto.builder().id(saved.getId()).name(saved.getName()).category(saved.getCategory()).build();
    }
}

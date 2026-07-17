package com.example.jobmatch.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkillDto {
    private Long id;
    private String name;
    private String category;
}

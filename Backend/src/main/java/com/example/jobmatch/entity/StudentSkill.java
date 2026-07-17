package com.example.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "student_skills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(StudentSkill.StudentSkillId.class)
public class StudentSkill {

    @Id
    @Column(name = "student_id")
    private Long studentId;

    @Id
    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "proficiency")
    private Integer proficiency;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentSkillId implements Serializable {
        private Long studentId;
        private Long skillId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StudentSkillId)) return false;
            StudentSkillId that = (StudentSkillId) o;
            return Objects.equals(studentId, that.studentId) && Objects.equals(skillId, that.skillId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(studentId, skillId);
        }
    }
}

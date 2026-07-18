package com.example.jobmatch.repository;

import com.example.jobmatch.entity.StudentSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface StudentSkillRepository extends JpaRepository<StudentSkill, StudentSkill.StudentSkillId> {

    @Query("SELECT ss.skillId FROM StudentSkill ss WHERE ss.studentId = :studentId")
    Set<Long> findSkillIdsByStudent(@Param("studentId") Long studentId);

    List<StudentSkill> findByStudentId(Long studentId);

    @Query("SELECT ss FROM StudentSkill ss WHERE ss.studentId IN :studentIds")
    List<StudentSkill> findByStudentIdIn(@Param("studentIds") Set<Long> studentIds);

    void deleteByStudentId(Long studentId);
}

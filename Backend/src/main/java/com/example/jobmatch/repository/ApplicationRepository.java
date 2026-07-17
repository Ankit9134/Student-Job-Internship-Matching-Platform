package com.example.jobmatch.repository;

import com.example.jobmatch.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStudentId(Long studentId);
    Optional<Application> findByStudentIdAndListingId(Long studentId, Long listingId);
}

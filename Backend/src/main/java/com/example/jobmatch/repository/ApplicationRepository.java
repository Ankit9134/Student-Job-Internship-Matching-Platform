package com.example.jobmatch.repository;

import com.example.jobmatch.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    @Query("SELECT a FROM Application a JOIN FETCH a.listing WHERE a.studentId = :studentId")
    List<Application> findByStudentIdWithListing(@Param("studentId") Long studentId);
    List<Application> findByStudentId(Long studentId);
    Optional<Application> findByStudentIdAndListingId(Long studentId, Long listingId);
    long countByListingId(Long listingId);

    @Query("SELECT a.listingId, COUNT(a) FROM Application a GROUP BY a.listingId")
    List<Object[]> countGroupedByListing();
}

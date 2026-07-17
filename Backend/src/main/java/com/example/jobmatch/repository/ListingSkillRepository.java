package com.example.jobmatch.repository;

import com.example.jobmatch.entity.ListingSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ListingSkillRepository extends JpaRepository<ListingSkill, ListingSkill.ListingSkillId> {
    List<ListingSkill> findByListingId(Long listingId);
    void deleteByListingId(Long listingId);
}

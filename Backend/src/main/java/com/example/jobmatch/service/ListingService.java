package com.example.jobmatch.service;

import com.example.jobmatch.dto.ListingDtos.ListingRequest;
import com.example.jobmatch.dto.ListingDtos.ListingResponse;
import com.example.jobmatch.dto.SkillDto;
import com.example.jobmatch.entity.Listing;
import com.example.jobmatch.entity.ListingSkill;
import com.example.jobmatch.entity.Skill;
import com.example.jobmatch.exception.ResourceNotFoundException;
import com.example.jobmatch.repository.ListingRepository;
import com.example.jobmatch.repository.ListingSkillRepository;
import com.example.jobmatch.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepo;
    private final ListingSkillRepository listingSkillRepo;
    private final SkillRepository skillRepo;

    @Transactional
    public Listing create(ListingRequest req) {
        Listing listing = Listing.builder()
            .title(req.getTitle())
            .companyName(req.getCompanyName())
            .description(req.getDescription())
            .location(req.getLocation())
            .workMode(req.getWorkMode())
            .minGpa(req.getMinGpa())
            .sponsorshipOffered(req.isSponsorshipOffered())
            .roleType(req.getRoleType())
            .createdBy(req.getCreatedBy())
            .active(true)
            .createdAt(Instant.now())
            .build();

        Listing saved = listingRepo.save(listing);
        saveSkillWeights(saved.getId(), req.getSkillWeights());
        return saved;
    }

    @Transactional
    public Listing update(Long id, ListingRequest req) {
        Listing listing = listingRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + id));

        listing.setTitle(req.getTitle());
        listing.setCompanyName(req.getCompanyName());
        listing.setDescription(req.getDescription());
        listing.setLocation(req.getLocation());
        listing.setWorkMode(req.getWorkMode());
        listing.setMinGpa(req.getMinGpa());
        listing.setSponsorshipOffered(req.isSponsorshipOffered());
        listing.setRoleType(req.getRoleType());

        Listing saved = listingRepo.save(listing);
        listingSkillRepo.deleteByListingId(id);
        saveSkillWeights(id, req.getSkillWeights());
        return saved;
    }

    private void saveSkillWeights(Long listingId, Map<Long, Integer> skillWeights) {
        if (skillWeights == null || skillWeights.isEmpty()) return;
        List<ListingSkill> entries = skillWeights.entrySet().stream()
            .map(e -> ListingSkill.builder()
                .listingId(listingId)
                .skillId(e.getKey())
                .weight(e.getValue())
                .build())
            .collect(Collectors.toList());
        listingSkillRepo.saveAll(entries);
    }

    @Transactional(readOnly = true)
    public Page<Listing> browse(Pageable pageable) {
        return listingRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Listing getById(Long id) {
        return listingRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + id));
    }

    @Transactional(readOnly = true)
    public ListingResponse toResponse(Listing listing) {
        List<ListingSkill> listingSkills = listingSkillRepo.findByListingId(listing.getId());
        Map<Long, Skill> skillMap = skillRepo.findAllById(
            listingSkills.stream().map(ListingSkill::getSkillId).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(Skill::getId, s -> s));

        List<SkillDto> required = listingSkills.stream()
            .filter(ls -> ls.getWeight() != null && ls.getWeight() >= 2)
            .map(ls -> toDto(skillMap.get(ls.getSkillId())))
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());

        List<SkillDto> niceToHave = listingSkills.stream()
            .filter(ls -> ls.getWeight() == null || ls.getWeight() < 2)
            .map(ls -> toDto(skillMap.get(ls.getSkillId())))
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());

        return ListingResponse.builder()
            .id(listing.getId())
            .title(listing.getTitle())
            .companyName(listing.getCompanyName())
            .description(listing.getDescription())
            .location(listing.getLocation())
            .workMode(listing.getWorkMode())
            .minGpa(listing.getMinGpa())
            .sponsorshipOffered(listing.isSponsorshipOffered())
            .roleType(listing.getRoleType())
            .requiredSkills(required)
            .niceToHaveSkills(niceToHave)
            .build();
    }

    private SkillDto toDto(Skill s) {
        return s == null ? null : SkillDto.builder().id(s.getId()).name(s.getName()).category(s.getCategory()).build();
    }
}

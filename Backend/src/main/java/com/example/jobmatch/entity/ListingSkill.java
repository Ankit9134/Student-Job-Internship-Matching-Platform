package com.example.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "listing_skills")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(ListingSkill.ListingSkillId.class)
public class ListingSkill {

    @Id
    @Column(name = "listing_id")
    private Long listingId;

    @Id
    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "weight")
    private Integer weight; // 1 = nice-to-have, 2 = required

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListingSkillId implements Serializable {
        private Long listingId;
        private Long skillId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ListingSkillId)) return false;
            ListingSkillId that = (ListingSkillId) o;
            return Objects.equals(listingId, that.listingId) && Objects.equals(skillId, that.skillId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(listingId, skillId);
        }
    }
}

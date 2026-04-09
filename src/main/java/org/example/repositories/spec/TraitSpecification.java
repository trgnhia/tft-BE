package org.example.repositories.spec;

import org.example.dto.trait.TraitFilterRequest;
import org.example.entities.trait.Trait;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class TraitSpecification {

    private TraitSpecification() {}

    public static Specification<Trait> of(TraitFilterRequest filter) {
        return Specification.allOf(
                hasKeyword(filter.getKeyword()),
                hasType(filter.getType()),
                hasSetId(filter.getSetId()),
                handleStatus(filter.getIsActive(), filter.getIncludeDeleted())
        );
    }

    private static Specification<Trait> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("slug")), pattern)
            );
        };
    }

    private static Specification<Trait> hasType(String type) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(type)) return null;
            return cb.equal(cb.lower(root.get("type")), type.toLowerCase());
        };
    }

    private static Specification<Trait> hasSetId(Long setId) {
        return (root, query, cb) -> {
            if (setId == null) return null;
            return cb.equal(root.get("sets").get("id"), setId);
        };
    }

    private static Specification<Trait> handleStatus(Boolean isActive, Boolean includeDeleted) {
        return (root, query, cb) -> {

            if (Boolean.TRUE.equals(includeDeleted)) {
                return null;
            }

            if (isActive != null) {
                return cb.equal(root.get("deleted"), !isActive);
            }

            return cb.equal(root.get("deleted"), false);
        };
    }
}
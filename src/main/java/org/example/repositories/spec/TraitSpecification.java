package org.example.repositories.spec;

import org.example.dto.trait.TraitFilterRequest;
import org.example.entities.trait.Trait;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.List;

public class TraitSpecification {

    private TraitSpecification() {}

    public static Specification<Trait> of(TraitFilterRequest filter) {
        return Specification.allOf(
                hasKeyword(filter.getKeyword()),
                hasType(filter.getType()),
                hasSetFilter(filter.getSetId(), filter.getSetIds()),
                hasStatus(filter.getStatus(), filter.getIsActive(), filter.getIncludeDeleted()),
                hasRestorable(filter.getRestorable())
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

    private static Specification<Trait> hasSetFilter(Long setId, List<Long> setIds) {
        return (root, query, cb) -> {
            List<Long> normalizedSetIds = setIds == null
                    ? List.of()
                    : setIds.stream().filter(java.util.Objects::nonNull).distinct().toList();

            if (!normalizedSetIds.isEmpty()) {
                return root.get("sets").get("id").in(normalizedSetIds);
            }

            if (setId == null) return null;
            return cb.equal(root.get("sets").get("id"), setId);
        };
    }

    private static Specification<Trait> hasStatus(String status, Boolean isActive, Boolean includeDeleted) {
        return (root, query, cb) -> {
            String normalizedStatus = status == null ? null : status.trim().toUpperCase();

            if ("ACTIVE".equals(normalizedStatus)) {
                return cb.isFalse(root.get("deleted"));
            }

            if ("INACTIVE".equals(normalizedStatus)) {
                return cb.isTrue(root.get("deleted"));
            }

            // Backward compatibility for existing isActive query param
            if (isActive != null) {
                return cb.equal(root.get("deleted"), !isActive);
            }

            // includeDeleted=true means no status restriction
            if (Boolean.TRUE.equals(includeDeleted)) {
                return null;
            }

            return cb.equal(root.get("deleted"), false);
        };
    }

    private static Specification<Trait> hasRestorable(Boolean restorable) {
        return (root, query, cb) -> {
            if (restorable == null) return null;

            if (Boolean.TRUE.equals(restorable)) {
                return cb.and(
                        cb.isTrue(root.get("deleted")),
                        cb.isFalse(root.get("sets").get("deleted"))
                );
            }

            return cb.and(
                    cb.isTrue(root.get("deleted")),
                    cb.isTrue(root.get("sets").get("deleted"))
            );
        };
    }
}

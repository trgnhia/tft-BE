package org.example.repositories.spec;

import org.example.entities.item.Item;
import org.springframework.data.jpa.domain.Specification;

public final class ItemSpecification {

    private ItemSpecification() {
    }

    public static Specification<Item> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return null;
            }

            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.like(
                    cb.lower(cb.coalesce(root.get("name"), "")),
                    pattern
            );
        };
    }

    public static Specification<Item> hasSetId(Long setId) {
        return (root, query, cb) -> {
            if (setId == null) {
                return null;
            }
            return cb.equal(root.get("sets").get("id"), setId);
        };
    }

    public static Specification<Item> hasSetDeleted(Boolean setDeleted) {
        return (root, query, cb) -> {
            if (setDeleted == null) {
                return null;
            }
            return cb.equal(root.get("sets").get("deleted"), setDeleted);
        };
    }

    public static Specification<Item> hasItemDeleted(Boolean itemDeleted) {
        return (root, query, cb) -> {
            if (itemDeleted == null) {
                return null;
            }
            return cb.equal(root.get("deleted"), itemDeleted);
        };
    }

    public static Specification<Item> hasTier(String tier) {
        return (root, query, cb) -> {
            if (tier == null || tier.isBlank()) {
                return null;
            }
            return cb.equal(cb.upper(root.get("tier")), tier.trim().toUpperCase());
        };
    }

    public static Specification<Item> publicVisible() {
        return (root, query, cb) -> cb.and(
                cb.isFalse(root.get("deleted")),
                cb.isFalse(root.get("sets").get("deleted"))
        );
    }
}
package org.example.repositories.spec;

import jakarta.persistence.criteria.Predicate;
import org.example.dto.champs.ChampFilterRequest;
import org.example.entities.champ.Champ;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ChampSpecification {

    private ChampSpecification() {}

    public static Specification<Champ> withFilter(ChampFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + filter.getKeyword().trim().toLowerCase() + "%"
                ));
            }
            if (filter.getSetId() != null) {
                predicates.add(cb.equal(root.get("sets").get("id"), filter.getSetId()));
            }
            if (filter.getCost() != null) {
                predicates.add(cb.equal(root.get("cost"), filter.getCost()));
            }
            if (filter.getTrait() != null && !filter.getTrait().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("trait")),
                        "%" + filter.getTrait().trim().toLowerCase() + "%"
                ));
            }
            if (filter.getTier() != null && !filter.getTier().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("tier")),
                        filter.getTier().trim().toLowerCase()
                ));
            }
            if (filter.getDeleted() != null) {
                predicates.add(cb.equal(root.get("deleted"), filter.getDeleted()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
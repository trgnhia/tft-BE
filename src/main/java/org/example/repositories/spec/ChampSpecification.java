package org.example.repositories.spec;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.example.dto.champs.ChampFilterRequest;
import org.example.entities.champ.Champ;
import org.example.entities.champ.ChampTrait;
import org.example.entities.trait.Trait;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ChampSpecification {

    private ChampSpecification() {}

    public static Specification<Champ> withFilter(ChampFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null) {
                query.distinct(true);
            }

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + filter.getKeyword().trim().toLowerCase() + "%"
                ));
            }
            if (filter.getSetId() != null) {
                predicates.add(cb.equal(root.get("sets").get("id"), filter.getSetId()));
            }
            if (Boolean.TRUE.equals(filter.getUnassignedSet())) {
                predicates.add(cb.isNull(root.get("sets")));
            } else if (Boolean.FALSE.equals(filter.getUnassignedSet())) {
                predicates.add(cb.isNotNull(root.get("sets")));
            }
            if (filter.getCost() != null) {
                predicates.add(cb.equal(root.get("cost"), filter.getCost()));
            }
            if (filter.getTrait() != null && !filter.getTrait().isBlank()) {
                Join<Champ, ChampTrait> champTraitJoin = root.join("champTraits", JoinType.LEFT);
                Join<ChampTrait, Trait> traitJoin = champTraitJoin.join("trait", JoinType.LEFT);
                List<Predicate> traitPredicates = new ArrayList<>();
                String[] traitTerms = filter.getTrait().split(",");
                for (String traitTerm : traitTerms) {
                    if (traitTerm == null || traitTerm.isBlank()) {
                        continue;
                    }
                    String normalizedTrait = "%" + traitTerm.trim().toLowerCase() + "%";
                    traitPredicates.add(cb.or(
                            cb.like(cb.lower(traitJoin.get("name")), normalizedTrait),
                            cb.like(cb.lower(traitJoin.get("slug")), normalizedTrait)
                    ));
                }
                if (!traitPredicates.isEmpty()) {
                    predicates.add(cb.or(traitPredicates.toArray(new Predicate[0])));
                }
                predicates.add(cb.equal(traitJoin.get("sets").get("id"), root.get("sets").get("id")));
            }
            if (filter.getDeleted() != null) {
                predicates.add(cb.equal(root.get("deleted"), filter.getDeleted()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

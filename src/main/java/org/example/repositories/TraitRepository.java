package org.example.repositories;

import org.example.entities.Trait;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TraitRepository extends JpaRepository<Trait, Long> {

    Optional<Trait> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
    Page<Trait> findBySlugContainingIgnoreCase(String keyword, Pageable pageable);
    List<Trait> findBySetId(Long setId);
    List<Trait> findByType(String type);
}
package org.example.repositories;

import org.example.entities.Champ;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChampRepository extends JpaRepository<Champ, Long> {
    Optional<Champ> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
    Page<Champ> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
    List<Champ> findBySets_Id(Long setId);
}

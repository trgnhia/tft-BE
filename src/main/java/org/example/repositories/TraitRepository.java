package org.example.repositories;

import org.example.entities.trait.Trait;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TraitRepository extends JpaRepository<Trait, Long> {

    boolean existsBySlug(String slug);

    Optional<Trait> findById(Long id);

    Optional<Trait> findBySlug(String slug);

    @Query("""
            SELECT t FROM Trait t
            WHERE (:keyword IS NULL
                OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Trait> findAllActive(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = """
            SELECT * FROM traits
            WHERE (:keyword IS NULL
                OR LOWER(name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(slug) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """, nativeQuery = true)
    Page<Trait> findAllAdmin(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM traits WHERE id = :id", nativeQuery = true)
    Optional<Trait> findByIdAdmin(@Param("id") Long id);
}
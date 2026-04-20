package org.example.repositories;

import jakarta.transaction.Transactional;
import org.example.entities.trait.Trait;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TraitRepository extends JpaRepository<Trait, Long>, JpaSpecificationExecutor<Trait> {

    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
    Optional<Trait> findById(Long id);

    Optional<Trait> findByIdAndDeletedFalse(Long id);
    Optional<Trait> findBySlugAndDeletedFalse(String slug);
    Optional<Trait> findBySlug(String slug);

    @Query("""
            SELECT t FROM Trait t
            WHERE t.deleted = false
            AND (:keyword IS NULL
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

    @Query("""
            SELECT t FROM Trait t
            WHERE t.id IN :ids
            """)
    List<Trait> findAllByIdAdmin(@Param("ids") List<Long> ids);


    @Query("""
            SELECT t.slug FROM Trait t
            WHERE t.slug IN :slugs
            """)
    List<String> findExistingSlugs(@Param("slugs") List<String> slugs);

    @Query("SELECT COUNT(t) FROM Trait t WHERE t.deleted = false")
    long countActive();

    @Query("SELECT COUNT(t) FROM Trait t WHERE t.deleted = true")
    long countDeleted();

    @Query("SELECT COUNT(t) FROM Trait t")
    long countTotal();

    @Query("SELECT COUNT(t) FROM Trait t WHERE t.deleted = true")
    long countInactive();

    @Modifying
    @Query("UPDATE Trait t SET t.deleted = false WHERE t.id = :id")
    void restoreById(@Param("id") Long id);

    @Query("""
            SELECT t FROM Trait t
            WHERE t.deleted = false
              AND (:setId IS NULL OR t.sets.id = :setId)
            ORDER BY t.name ASC
            """)
    List<Trait> findAllActiveForDropdown(@Param("setId") Long setId);

    @Query(value = """
    SELECT * FROM traits t
    WHERE (:keyword IS NULL 
        OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
        OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND (:type IS NULL OR LOWER(t.type) = LOWER(:type))
    AND (:setId IS NULL OR t.set_id = :setId)
    """,
            countQuery = """
    SELECT count(*) FROM traits t
    WHERE (:keyword IS NULL 
        OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
        OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND (:type IS NULL OR LOWER(t.type) = LOWER(:type))
    AND (:setId IS NULL OR t.set_id = :setId)
    """,
            nativeQuery = true)
    Page<Trait> searchAdminIncludeDeleted(
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("setId") Long setId,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("UPDATE Trait t SET t.deleted = false WHERE t.id IN :ids")
    void restoreAllByIds(@Param("ids") List<Long> ids);
}

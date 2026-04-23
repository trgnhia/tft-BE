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

    @Query(value = "SELECT * FROM traits WHERE id IN (:ids)", nativeQuery = true)
    List<Trait> findAllByIdsAdmin(@Param("ids") List<Long> ids);

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
            SELECT DISTINCT TRIM(t.type)
            FROM traits t
            WHERE t.type IS NOT NULL
              AND TRIM(t.type) <> ''
            ORDER BY LOWER(TRIM(t.type))
            """, nativeQuery = true)
    List<String> findDistinctTypesForCms();

    @Query(value = """
    SELECT t.* FROM traits t
    LEFT JOIN "set" s ON t.set_id = s.id
    WHERE (:keyword IS NULL
        OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:type IS NULL OR LOWER(t.type) = LOWER(:type))
      AND (
          (:applySetIds = true AND t.set_id IN (:setIds))
          OR (:applySetIds = false AND :setId IS NULL)
          OR (:applySetIds = false AND :setId IS NOT NULL AND t.set_id = :setId)
      )
      AND (
          :status IS NULL
          OR (UPPER(:status) = 'ACTIVE' AND t.deleted = false)
          OR (UPPER(:status) = 'INACTIVE' AND t.deleted = true)
      )
      AND (
          :restorable IS NULL
          OR (
              :restorable = true
              AND t.deleted = true
              AND COALESCE(s.deleted, false) = false
          )
          OR (
              :restorable = false
              AND t.deleted = true
              AND COALESCE(s.deleted, false) = true
          )
      )
    """,
            countQuery = """
    SELECT count(*) FROM traits t
    LEFT JOIN "set" s ON t.set_id = s.id
    WHERE (:keyword IS NULL
        OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(t.slug) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:type IS NULL OR LOWER(t.type) = LOWER(:type))
      AND (
          (:applySetIds = true AND t.set_id IN (:setIds))
          OR (:applySetIds = false AND :setId IS NULL)
          OR (:applySetIds = false AND :setId IS NOT NULL AND t.set_id = :setId)
      )
      AND (
          :status IS NULL
          OR (UPPER(:status) = 'ACTIVE' AND t.deleted = false)
          OR (UPPER(:status) = 'INACTIVE' AND t.deleted = true)
      )
      AND (
          :restorable IS NULL
          OR (
              :restorable = true
              AND t.deleted = true
              AND COALESCE(s.deleted, false) = false
          )
          OR (
              :restorable = false
              AND t.deleted = true
              AND COALESCE(s.deleted, false) = true
          )
      )
    """,
            nativeQuery = true)
    Page<Trait> searchAdminIncludeDeleted(
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("applySetIds") boolean applySetIds,
            @Param("setIds") List<Long> setIds,
            @Param("setId") Long setId,
            @Param("status") String status,
            @Param("restorable") Boolean restorable,
            Pageable pageable
    );

    @Modifying
    @Transactional
    @Query("UPDATE Trait t SET t.deleted = false WHERE t.id IN :ids")
    void restoreAllByIds(@Param("ids") List<Long> ids);
}

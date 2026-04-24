package org.example.repositories;

import org.example.entities.champ.Champ;
import org.example.repositories.projection.ChampImportLookup;
import org.example.repositories.projection.ChampRestoreCandidate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChampRepository extends JpaRepository<Champ, Long>, JpaSpecificationExecutor<Champ> {
    @EntityGraph(attributePaths = {"champTraits", "champTraits.trait"})
    Optional<Champ> findBySlug(String slug);

    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    @EntityGraph(attributePaths = {"champTraits", "champTraits.trait"})
    Page<Champ> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = {"champTraits", "champTraits.trait"})
    List<Champ> findBySets_Id(Long setId);

    @EntityGraph(attributePaths = {"champTraits", "champTraits.trait"})
    Optional<Champ> findById(Long id);

    @EntityGraph(attributePaths = {"champTraits", "champTraits.trait"})
    Page<Champ> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"champTraits", "champTraits.trait"})
    Page<Champ> findAll(Specification<Champ> spec, Pageable pageable);

    long countByDeletedFalse();

    long countByDeletedTrue();

    @Query("SELECT c.sets.name, COUNT(c) FROM Champ c GROUP BY c.sets.name")
    List<Object[]> countGroupBySet();

    @Query("SELECT c.cost, COUNT(c) FROM Champ c WHERE c.deleted = false GROUP BY c.cost")
    List<Object[]> countGroupByCost();

    @EntityGraph(attributePaths = {"champTraits", "champTraits.trait"})
    List<Champ> findBySetsId(Long setId);

    @EntityGraph(attributePaths = {"champTraits", "champTraits.trait"})
    List<Champ> findAllByDeletedFalseOrderByNameAsc();

    @EntityGraph(attributePaths = {"champTraits", "champTraits.trait"})
    List<Champ> findAllBySetsIdAndDeletedFalseOrderByNameAsc(Long setId);

    @Query(value = "select slug from champs", nativeQuery = true)
    List<String> findAllSlugsIncludingDeleted();

    @Query(value = """
            select c.id as champId,
                   c.code as code,
                   c.deleted as deleted
            from champs c
            """, nativeQuery = true)
    List<ChampImportLookup> findAllCodeLookupIncludingDeleted();

    @Query(value = "select exists(select 1 from champs c where lower(c.slug) = lower(:slug))", nativeQuery = true)
    boolean existsBySlugIncludingDeleted(@Param("slug") String slug);

    @Query(value = "select exists(select 1 from champs c where lower(c.slug) = lower(:slug) and c.id <> :id)", nativeQuery = true)
    boolean existsBySlugAndIdNotIncludingDeleted(@Param("slug") String slug, @Param("id") Long id);

    @Query(value = "select exists(select 1 from champs c where lower(c.code) = lower(:code))", nativeQuery = true)
    boolean existsByCodeIncludingDeleted(@Param("code") String code);

    @Query(value = "select exists(select 1 from champs c where lower(c.code) = lower(:code) and c.id <> :id)", nativeQuery = true)
    boolean existsByCodeAndIdNotIncludingDeleted(@Param("code") String code, @Param("id") Long id);

    @Query(value = "select count(*) from champs c where c.image_url = :imageUrl", nativeQuery = true)
    long countByImageUrlIncludingDeleted(@Param("imageUrl") String imageUrl);

    @Query("""
            select c.id as champId,
                   c.deleted as champDeleted,
                   s.id as setId,
                   s.deleted as setDeleted
            from Champ c
            left join c.sets s
            where c.id in :ids
            """)
    List<ChampRestoreCandidate> findRestoreCandidatesByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query("""
            update Champ c
            set c.deleted = false
            where c.id in :ids
            """)
    int bulkRestoreByIds(@Param("ids") List<Long> ids);
}

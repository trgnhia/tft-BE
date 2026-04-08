package org.example.repositories;

import org.example.entities.ChampItemRecommend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChampItemRecommendRepository extends JpaRepository<ChampItemRecommend, Long> {
    Optional<ChampItemRecommend> findByIdAndDeletedFalse(Long id);
    boolean existsByChampionIdAndItemIdAndDeletedFalse(Long championId, Long itemId);
    boolean existsByChampionIdAndItemIdAndIdNotAndDeletedFalse(Long championId, Long itemId, Long id);
    @Query("""
    select c from ChampItemRecommend c
    join fetch c.item
    where c.deleted = false
    order by c.priority asc, c.id desc
""")
    List<ChampItemRecommend> findAllActive();

    @Query("""
    select c from ChampItemRecommend c
    join fetch c.item
    where c.deleted = false
      and c.championId = :championId
    order by c.priority asc, c.id desc
""")
    List<ChampItemRecommend> findAllByChampionId(@Param("championId") Long championId);

    @Modifying
    @Query("""
                update ChampItemRecommend c
                set c.deleted = true
                where c.championId = :championId
            """)
    void softDeleteByChampionId(@Param("championId") Long championId);

    @Modifying
    @Query("""
                update ChampItemRecommend c
                set c.deleted = true
                where c.item.id = :itemId
            """)
    void softDeleteByItemId(@Param("itemId") Long itemId);

    @Modifying
    @Query("""
                update ChampItemRecommend c
                set c.deleted = true
                where c.item.sets.id = :setId
            """)
    void softDeleteBySetId(@Param("setId") Long setId);
}

package org.example.repositories;

import org.example.entities.ChampItemRecommend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChampItemRecommendRepository extends JpaRepository<ChampItemRecommend, Long> {




    @Modifying
    @Query("""
                update ChampItemRecommend c
                set c.deleted = true
                where c.id = :id
            """)
    void softDeleteById(@Param("id") Long id);

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

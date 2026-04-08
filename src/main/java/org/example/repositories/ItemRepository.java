package org.example.repositories;

import org.example.entities.item.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    boolean existsByName (String name);
    boolean existsByNameAndIdNot (String name, Long id);

    @Query("select i from Item i join fetch i.sets where i.deleted = false")
    List<Item> findAllActiveWithSets();

    @Query("select i from Item i join fetch i.sets")
    List<Item> findAllWithSets();
    Optional<Item> findByIdAndDeletedFalse(Long id);

    @Modifying
    @Query("""
        update Item i
        set i.deleted = true
        where i.sets.id = :setId
    """)
    void softDeleteBySetId(@Param("setId") Long setId);

    @Query("""
        select i from Item i
        where i.deleted = false
          and (:keyword is null or lower(i.name) like lower(concat('%', :keyword, '%')))
          and (:setId is null or i.sets.id = :setId)
    """)
    Page<Item> searchItems(@Param("keyword") String keyword,
                           @Param("setId") Long setId,
                           Pageable pageable);

}

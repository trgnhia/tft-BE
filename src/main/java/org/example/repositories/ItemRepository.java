package org.example.repositories;

import org.example.entities.item.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> , JpaSpecificationExecutor<Item> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Query("""
                select i from Item i
                join fetch i.sets s
                where i.deleted = false
                  and s.deleted = false
            """)
    List<Item> findAllPublishedWithSets();

    @Query("""
                select i from Item i
                join fetch i.sets s
                where i.id = :id
                  and i.deleted = false
                  and s.deleted = false
            """)
    Optional<Item> findPublishedById(@Param("id") Long id);


    @Query("select i from Item i join fetch i.sets")
    List<Item> findAllWithSets();
}

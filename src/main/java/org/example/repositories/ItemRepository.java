package org.example.repositories;

import org.example.entities.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}

package org.example.repositories;

import org.example.entities.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    boolean existsByName (String name);

    @Query("select i from Item i join fetch i.sets")
    List<Item> findAllWithSets();
}

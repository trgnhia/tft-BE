package org.example.repositories;

import org.example.entities.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Boolean existsByName (String name);
}

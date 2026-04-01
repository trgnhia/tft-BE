package org.example.repositories;

import org.example.entities.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SetRepository extends JpaRepository<Set, Long> {
    boolean existsByName (String name);

}

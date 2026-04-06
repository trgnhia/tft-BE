package org.example.repositories;

import org.example.entities.Sets;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SetsRepository extends JpaRepository<Sets, Long> {
    boolean existsByName (String name);
    boolean existsByNameAndIdNot (String name, Long id);
}

package org.example.repositories;

import org.example.entities.Sets;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SetsRepository extends JpaRepository<Sets, Long> {
    boolean existsByName (String name);
    boolean existsByNameAndIdNot (String name, Long id);
    List<Sets> findAllByDeletedFalse();
    @Query("""
    select s from Sets s
    where (:deleted is null or s.deleted = :deleted)
""")
    Page<Sets> searchSetsForCms(@Param("deleted") Boolean deleted, Pageable pageable);

    List<Sets> findAllByIdInAndDeletedFalse(List<Long> ids);
}

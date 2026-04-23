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
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
    List<Sets> findAllByDeletedFalse();
    List<Sets> findAllByDeletedFalseOrderByNameAsc();
    List<Sets> findAllByOrderByNameAsc();
    @Query("""
    select s from Sets s
    where (:deleted is null or s.deleted = :deleted)
      and (
            :keyword = '' 
            or lower(s.name) like lower(concat('%', :keyword, '%'))
            or lower(coalesce(s.code, '')) like lower(concat('%', :keyword, '%'))
            or lower(coalesce(s.description, '')) like lower(concat('%', :keyword, '%'))
      )
""")
    Page<Sets> searchSetsForCms(@Param("keyword") String keyword,
                                @Param("deleted") Boolean deleted,
                                Pageable pageable);

    List<Sets> findAllByIdInAndDeletedFalse(List<Long> ids);
}

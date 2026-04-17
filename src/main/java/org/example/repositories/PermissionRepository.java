package org.example.repositories;

import org.example.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    @Query("SELECT p from Permission p WHERE :keyword IS NULL OR :keyword = '' OR p.code LIKE UPPER(CONCAT('%', :keyword,'%'))")
    List<Permission> findWithKeyword(@Param("keyword") String keyword);

    @Query("""
                SELECT COUNT(p) > 0
                FROM Permission p
                WHERE p.code = :code
            """)
    boolean existsByCode(@Param("code") String code);
}

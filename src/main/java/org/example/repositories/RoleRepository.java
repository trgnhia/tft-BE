package org.example.repositories;

import org.example.entities.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query("SELECT r FROM Role r WHERE r.code=:roleCode AND r.deleted = false")
    Optional<Role> findByCode(@Param("code") String roleCode);

    @Query("Select r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id=:id AND r.deleted = false")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);

    boolean existsByCode(String roleCode);

    boolean existsByName(String roleName);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.code=:keyword AND r.deleted = false")
    Page<Role> findAllNonDeleteWithKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.deleted = false")
    Page<Role> findAllNonDelete(Pageable pageable);
}

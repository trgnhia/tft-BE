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
    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.code=:roleCode")
    Optional<Role> findByCode(@Param("code") String roleCode);

    @Query("Select r FROM Role r JOIN FETCH r.permissions WHERE r.id=:id")
    Optional<Role> findByIdWithPermissions(@Param("id") Long id);

    boolean existsByCode(String roleCode);

    boolean existsByName(String roleName);

    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.code=:keyword")
    Page<Role> findAllWithKeyword(@Param("keyword") String keyword, Pageable pageable);

}

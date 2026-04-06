package org.example.repositories;

import org.example.common.enums.RoleCode;
import org.example.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query("SELECT r FROM Role r JOIN FETCH r.permissions WHERE r.code=:roleCode")
    Optional<Role> findByCode(@Param("roleCode") RoleCode roleCode);
}

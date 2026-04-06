package org.example.repositories;

import jakarta.validation.constraints.Email;
import lombok.NonNull;
import org.example.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u JOIN FETCH u.role r JOIN FETCH r.permissions WHERE u.username = :username")
    Optional<User> findByUsernameWithRolesAndPermissions(@Param("username") String username);

    @Query("SELECT u FROM User u JOIN FETCH u.role r JOIN FETCH r.permissions WHERE u.id = :id")
    Optional<User> findById(@Param("id") Long id);

    boolean existsByUsername(@NonNull String username);

    boolean existsByEmail(@NonNull @Email String email);

    @Query("""
                SELECT u FROM User u JOIN FETCH u.role r JOIN FETCH r.permissions
                WHERE u.deleted = false
                AND (CAST(:userName AS STRING) IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:userName AS STRING), '%')))
                AND (CAST(:email AS STRING) IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:email AS STRING), '%')))
                AND (CAST(:roleId AS LONG) IS NULL OR u.role.id = :roleId)
                AND (CAST(:enable AS BOOLEAN) IS NULL OR u.enabled = :enable)
            """)
    Page<User> findAllByFilter(@Param("userName") String userName, @Param("email") String email,
                               @Param("roleId") Long roleId, @Param("enable") Boolean enable, Pageable pageable);
}

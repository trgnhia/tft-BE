package org.example.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.common.entity.AuditableEntity;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends AuditableEntity {
    @Column(name = "username", unique = true)
    private String username;
    @Column(name = "email", unique = true)
    private String email;
    @Column(name = "password_hash")
    private String passwordHash;
    @Column(name = "enabled")
    private boolean enabled;
    @Column(name = "last_logout_at")
    private Instant lastLogoutAt;
    @ManyToOne(fetch = FetchType.EAGER)
    private Role role;
}

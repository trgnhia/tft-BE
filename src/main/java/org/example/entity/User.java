package org.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.example.common.entity.AuditableEntity;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends AuditableEntity {
    private String username;
    private String email;
    private String passwordHash;
    private boolean enabled;
    Instant lastLogoutAt;
}

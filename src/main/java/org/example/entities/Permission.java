package org.example.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.example.common.entity.AuditableEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permission extends AuditableEntity {
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();
}

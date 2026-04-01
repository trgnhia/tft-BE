package org.example.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.common.entity.AuditableEntity;

@Entity
@Getter
@Setter
@Table(name = "set")
@NoArgsConstructor
@AllArgsConstructor
public class Set extends AuditableEntity {
    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    @Column(nullable = false, length = 100)
    private String name;
}

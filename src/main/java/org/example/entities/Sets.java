package org.example.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.common.entity.AuditableEntity;

@Entity
@Getter
@Setter
@Table(name = "set")
@NoArgsConstructor
@AllArgsConstructor
public class
Sets extends AuditableEntity {
    @Column(nullable = false, length = 100)
    private String name;
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "created_by", insertable = false, updatable = false)
    private User createdByUser;
}

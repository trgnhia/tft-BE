package org.example.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.common.entity.AuditableEntity;

@Entity
@Table(name = "items")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Item extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private Sets sets;
    @Column(name = "image_url")
    private String imageUrl;
}

package org.example.entities.item;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.example.common.entity.AuditableEntity;
import org.example.entities.Sets;
import org.hibernate.annotations.Type;

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
    private String name;
    @Column(name = "image_url")
    private String imageUrl;
    @Type(JsonBinaryType.class)
    @Column(name = "stats", columnDefinition = "jsonb")
    private ItemStats stats;
    @Type(JsonBinaryType.class)
    @Column(name = "effects", columnDefinition = "jsonb")
    private ItemEffects effects;
    @Column(name = "description", columnDefinition = "text")
    private String description;

}

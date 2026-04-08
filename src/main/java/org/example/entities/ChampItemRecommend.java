package org.example.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.common.entity.BaseEntity;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "champ_item_recommend")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
public class ChampItemRecommend extends BaseEntity {
    @Column(name = "champion_id", nullable = false)
    private Long championId;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
}

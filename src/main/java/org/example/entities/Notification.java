package org.example.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.common.entity.BaseEntity;

import java.time.Instant;

@Entity
@Table(name = "notification")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends BaseEntity {
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    @Column(name = "content", nullable = false, length = 1000)
    private String content;
    @Column(name = "target_id", nullable = false)
    private Long targetId;
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

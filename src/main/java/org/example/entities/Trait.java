package org.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.example.common.entity.AuditableEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "traits")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLRestriction("deleted = false")
@SQLDelete(sql = "UPDATE traits SET deleted = true WHERE id = ?")
public class Trait extends AuditableEntity {


}

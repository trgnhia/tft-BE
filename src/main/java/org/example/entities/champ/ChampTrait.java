package org.example.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.common.entity.BaseEntity;

@Entity
@Table(
        name = "champ_traits",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_champ_trait",
                columnNames = {"champion_id", "trait_id"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChampTrait extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_champ_trait_champ"))
    private Champ champ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trait_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_champ_trait_trait"))
    private Trait trait;

}

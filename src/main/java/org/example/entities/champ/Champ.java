package org.example.entities;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.common.entity.AuditableEntity;
import org.example.entities.champ.ChampStats;
import org.example.entities.champ.ChampTrait;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "champs")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE champs SET deleted = true WHERE id = ?")
@FilterDef(name = "deletedFilter", parameters = @ParamDef(name = "deleted", type = Boolean.class))
@Filter(name = "deletedFilter", condition = "deleted = :deleted")
public class Champ extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_champ_set"))
    private Sets sets;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stats", columnDefinition = "jsonb")
    private ChampStats stats;

    @OneToMany(mappedBy = "champ", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ChampTrait> champTraits = new ArrayList<>();
}

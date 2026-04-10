package org.example.entities.trait;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.*;
import org.example.common.entity.AuditableEntity;
import org.example.entities.Sets;
import org.example.entities.champ.ChampTrait;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;


import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "traits")
@Getter
@Setter
@SQLDelete(sql = "UPDATE traits SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class Trait extends AuditableEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "breakpoint", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<TraitBreakpoint> breakpoints;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id", nullable = false)
    private Sets sets;

    @OneToMany(mappedBy = "trait", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ChampTrait> champTraits = new ArrayList<>();
}

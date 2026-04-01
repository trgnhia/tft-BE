package org.example.entities;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.*;
import org.example.common.entity.AuditableEntity;
import org.hibernate.annotations.*;


import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "traits")
@Getter
@Setter
@FilterDef(name = "deletedFilter", parameters = @ParamDef(name = "deleted", type = Boolean.class))
@Filter(name = "deletedFilter", condition = "deleted = :deleted")
@SQLDelete(sql = "UPDATE traits SET deleted = true WHERE id = ?")
public class Trait extends AuditableEntity {
    @Column(name = "set_id", nullable = false)
    private Long setId;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "breakpoint", columnDefinition = "json")
    private String breakpoint;

    @OneToMany(mappedBy = "trait", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ChampTrait> champTraits = new ArrayList<>();
}

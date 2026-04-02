package org.example.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.common.entity.AuditableEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "team_comp")
@Getter
@Setter
public class TeamComp extends AuditableEntity {

    @Column(name = "set_id")
    private Long setId;

    @Column(name = "slug", unique = true)
    private String slug;

    @Column(name = "style")
    private String style;

    @Column(name = "name")
    private String name;

    @Column(name = "tier", length = 10)
    private String tier;

    @OneToMany(mappedBy = "teamComp", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamCompChamp> teamCompChamps = new ArrayList<>();

}
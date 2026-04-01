package org.example.entities;


import jakarta.persistence.*;
import lombok.*;
import org.example.common.entity.AuditableEntity;

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

}
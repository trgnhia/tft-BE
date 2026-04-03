package org.example.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "team_comp_champ")
@Getter
@Setter
@IdClass(TeamCompChamp.TeamCompChampId.class)
public class TeamCompChamp {

    @Id
    @Column(name = "team_comp_id")
    private Long teamCompId;

    @Id
    @Column(name = "champion_id")
    private Long championId;

    // Liên kết với bảng team_comp
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_comp_id", insertable = false, updatable = false)
    private TeamComp teamComp;

    // Liên kết với bảng champs
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "champion_id", insertable = false, updatable = false)
    private Champ champ;

    // Định nghĩa Composite Primary Key Class
    @Getter
    @Setter
    @EqualsAndHashCode
    public static class TeamCompChampId implements Serializable {
        private Long teamCompId;
        private Long championId;
    }
}
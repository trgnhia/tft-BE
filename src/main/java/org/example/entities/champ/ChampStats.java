package org.example.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChampStats implements Serializable {

    private int cost;
    private List<Integer> hp;
    private List<Integer> ad;
    private int armor;
    private int range;

    @JsonProperty("magic_resist")
    private int magicResist;

    @JsonProperty("attack_speed")
    private double attackSpeed;

    @JsonProperty("crit_chance")
    private double critChance;


}

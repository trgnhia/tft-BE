package org.example.dto.champs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChampStatsResponse {
    private List<Integer> hp;
    private List<Integer> ad;
    private Integer armor;
    private Integer range;

    @JsonProperty("magic_resist")
    private int magicResist;

    @JsonProperty("attack_speed")
    private double attackSpeed;

    @JsonProperty("crit_chance")
    private double critChance;
}

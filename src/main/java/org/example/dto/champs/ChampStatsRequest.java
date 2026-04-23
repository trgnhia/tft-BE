package org.example.dto.champs;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.constant.Constants;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChampStatsRequest {

    @NotEmpty(message = "{" + Constants.MessageKey.CHAMP_STATS_HP_NOT_EMPTY + "}")
    @Size(min = 1, message = "{" + Constants.MessageKey.CHAMP_STATS_HP_SIZE + "}")
    private List<@Min(value = 1, message = "{" + Constants.MessageKey.CHAMP_STATS_HP_MIN + "}") Integer> hp;

    @NotEmpty(message = "{" + Constants.MessageKey.CHAMP_STATS_AD_NOT_EMPTY + "}")
    @Size(min = 3, max = 3, message = "{" + Constants.MessageKey.CHAMP_STATS_AD_SIZE + "}")
    private List<@Min(value = 0, message = "{" + Constants.MessageKey.CHAMP_STATS_AD_MIN + "}") Integer> ad;

    @Min(value = 0, message = "{" + Constants.MessageKey.CHAMP_STATS_ARMOR_MIN + "}")
    private int armor;

    @Min(value = 1, message = "{" + Constants.MessageKey.CHAMP_STATS_RANGE_MIN + "}")
    private int range;

    @JsonProperty("magic_resist")
    @JsonAlias("magicResist")
    @Min(value = 0, message = "{" + Constants.MessageKey.CHAMP_STATS_MAGIC_RESIST_MIN + "}")
    private int magicResist;

    @JsonProperty("attack_speed")
    @JsonAlias("attackSpeed")
    @DecimalMin(value = "0.1", message = "{" + Constants.MessageKey.CHAMP_STATS_ATTACK_SPEED_MIN + "}")
    private double attackSpeed;

    @JsonProperty("crit_chance")
    @JsonAlias("critChance")
    @DecimalMin(value = "0.0", message = "{" + Constants.MessageKey.CHAMP_STATS_CRIT_CHANCE_MIN + "}")
    @DecimalMax(value = "1.0", message = "{" + Constants.MessageKey.CHAMP_STATS_CRIT_CHANCE_MAX + "}")
    private double critChance;
}

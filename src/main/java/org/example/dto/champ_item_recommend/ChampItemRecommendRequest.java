package org.example.dto.champ_item_recommend;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChampItemRecommendRequest {
    @NotNull(message = "{champ_item_recommend.champion_id.not_null}")
    @Min(value = 1, message = "{champ_item_recommend.champion_id.min}")
    private Long championId;

    @NotNull(message = "{champ_item_recommend.item_id.not_null}")
    @Min(value = 1, message = "{champ_item_recommend.item_id.min}")
    private Long itemId;

    @Min(value = 1, message = "{champ_item_recommend.priority.min}")
    private Integer priority;
}
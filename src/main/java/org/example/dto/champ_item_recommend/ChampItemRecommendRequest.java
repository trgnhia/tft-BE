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

    @NotNull(message = "championId must not be null")
    @Min(value = 1, message = "championId must be greater than 0")
    private Long championId;

    @NotNull(message = "itemId must not be null")
    @Min(value = 1, message = "itemId must be greater than 0")
    private Long itemId;

    @Min(value = 1, message = "priority must be greater than 0")
    private Integer priority;

}
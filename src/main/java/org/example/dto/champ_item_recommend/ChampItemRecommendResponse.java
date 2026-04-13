package org.example.dto.champ_item_recommend;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChampItemRecommendResponse {
    private Long id;
    private Long championId;
    private Long itemId;
    private String itemName;
    private Integer priority;
    private Long createdBy;
    private Instant createdAt;
}
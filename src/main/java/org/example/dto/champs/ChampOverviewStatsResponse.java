package org.example.dto.champs;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ChampOverviewStatsResponse {
    private long totalChamps;
    private long totalDeleted;
    private long totalActive;
    private Map<String, Long> countBySet;
    private Map<Integer, Long> countByCost;
    private Map<String, Long> countByTrait;
    private Map<String, Long> countByTier;
}

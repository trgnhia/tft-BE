package org.example.dto.trait;

import lombok.Data;

@Data
public class TraitOverviewStatsResponse {
    private long total;
    private long active;
    private long inactive;
    private long deleted;
}
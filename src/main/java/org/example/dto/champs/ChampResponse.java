package org.example.dto.champs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChampResponse {
    private Long id;
    private Long setsId;
    private Integer cost;
    private String setsName;
    private Boolean setDeleted;
    private String slug;
    private String name;
    private String imageUrl;
    private List<ChampTraitResponse> traits;
    private ChampStatsResponse stats;
    private Boolean canRestore;
    private String restoreBlockedReason;
    private Boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;
}

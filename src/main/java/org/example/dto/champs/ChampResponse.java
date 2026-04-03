package org.example.dto.champs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChampResponse {
    private Long id;
    private Long setsId;
    private String setsName;
    private String slug;
    private String name;
    private String imageUrl;
    private ChampStatsResponse stats;
    private Boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;
}

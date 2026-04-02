package org.example.dto.champs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChampResponse {
    private Long id;
    private Long setsId;
    private String setsName;
    private String slug;
    private String name;
    private String imageUrl;
    private String stats;
    private Boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;
}

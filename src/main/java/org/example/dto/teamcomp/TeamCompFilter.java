package org.example.dto.teamcomp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamCompFilter {
    private Long setId;
    private String keyword;
    private List<String> styles;
    private List<String> tiers;
    private Long championId;
    private Boolean deleted;
    private Boolean setDeleted;
}
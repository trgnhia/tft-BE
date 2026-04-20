package org.example.dto.champs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChampTraitResponse {
    private Long id;
    private String name;
    private String type;
}

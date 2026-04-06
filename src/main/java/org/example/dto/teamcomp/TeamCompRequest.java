package org.example.dto.teamcomp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class TeamCompRequest {

    @NotNull(message = "{team.comp.setId.not_null}")
    private Long setId;

    @NotBlank(message = "{team.comp.tier.not_blank}")
    @Size(max = 10, message = "{team.comp.tier.size}")
    private String tier;

    @NotBlank(message = "{team.comp.name.not_blank}")
    @Size(max = 255, message = "{team.comp.name.size}")
    private String name;

    @NotBlank(message = "{team.comp.style.not_blank}")
    @Size(max = 255, message = "{team.comp.style.size}")
    private String style;

    @NotEmpty(message = "{team.comp.champIds.not_empty}")
    @Size(max = 15, message = "{team.comp.champIds.size}")
    private List<Long> championIds;
}
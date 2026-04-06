package org.example.dto.teamcomp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TeamCompRequest {

    @NotNull(message = "team.comp.setId.not_null")
    private Long setId;

    @NotBlank(message = "team.comp.tier.not_blank")
    private String tier;

    @NotBlank(message = "team.comp.name.not_blank")
    private String name;

    @NotBlank(message = "team.comp.style.not_blank")
    private String style;

    @NotEmpty(message = "team.comp.champIds.not_empty")
    private List<Long> championIds;
}
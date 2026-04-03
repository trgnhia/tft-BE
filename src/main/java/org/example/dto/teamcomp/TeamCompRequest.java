package org.example.dto.teamcomp;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TeamCompRequest {

    private String tier;         //  "S", "A", "B"
    private String name;         //  "Mecha Channelers"
    private String style;        //  "Fast 8", "Slow Roll (7)"


    private List<Long> championIds;
}
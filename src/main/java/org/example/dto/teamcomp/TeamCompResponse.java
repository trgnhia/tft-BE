package org.example.dto.teamcomp;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class TeamCompResponse {

    private Long id;
    private String tier;
    private String name;
    private String style;
    private boolean deleted;
    private SetSimpleDto set;

    private List<ChampionSimpleDto> champions;

    @Getter
    @Setter
    public static class SetSimpleDto {
        private Long id;
        private String name;
        private boolean deleted;
    }

    @Getter
    @Setter
    public static class ChampionSimpleDto {
        private Long id;
        private String name;
        private String avatarUrl;
        private Integer cost;
    }
}
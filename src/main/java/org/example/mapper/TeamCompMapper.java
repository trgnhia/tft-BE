package org.example.mapper;

import org.example.dto.teamcomp.TeamCompRequest;
import org.example.dto.teamcomp.TeamCompResponse;
import org.example.entities.TeamComp;
import org.example.entities.TeamCompChamp;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TeamCompMapper {

    // 1. MAPPING ENTITY -> RESPONSE
    @Mapping(target = "champions", source = "teamCompChamps")
    TeamCompResponse toResponse(TeamComp entity);

    @Mapping(target = "id", source = "champ.id")
    @Mapping(target = "name", source = "champ.name")
    @Mapping(target = "avatarUrl", source = "champ.imageUrl")
    @Mapping(target = "cost", source = "champ.cost")
    TeamCompResponse.ChampionSimpleDto toChampionSimpleDto(TeamCompChamp tcc);

    // 2. MAPPING REQUEST -> ENTITY
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teamCompChamps", ignore = true)
    @Mapping(target = "setId", constant = "17L") // Tạm hardcode Set 17
    @Mapping(target = "slug", expression = "java(generateSlug(request.getName()))")
    TeamComp toEntity(TeamCompRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teamCompChamps", ignore = true)
    @Mapping(target = "setId", ignore = true)
    @Mapping(target = "slug", expression = "java(generateSlug(request.getName()))")
    void updateEntity(TeamCompRequest request, @MappingTarget TeamComp entity);

    // 3. CÁC HÀM XỬ LÝ CUSTOM
    default String generateSlug(String name) {
        if (name == null || name.isBlank()) return null;
        return name.toLowerCase().replaceAll("\\s+", "-");
    }
}
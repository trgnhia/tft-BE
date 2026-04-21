package org.example.mapper;

import org.example.dto.teamcomp.TeamCompRequest;
import org.example.dto.teamcomp.TeamCompResponse;
import org.example.entities.TeamComp;
import org.example.entities.TeamCompChamp;
import org.example.entities.Sets;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TeamCompMapper {

    // MAPPING ENTITY -> RESPONSE
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "champions", source = "teamCompChamps")
    @Mapping(target = "set", source = "sets")
    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "updatedAt", source = "updatedAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
    TeamCompResponse toResponse(TeamComp entity);

    TeamCompResponse.SetSimpleDto toSetSimpleDto(Sets sets);

    @Mapping(target = "id", source = "champ.id")
    @Mapping(target = "name", source = "champ.name")
    @Mapping(target = "avatarUrl", source = "champ.imageUrl")
    @Mapping(target = "cost", source = "champ.cost")
    TeamCompResponse.ChampionSimpleDto toChampionSimpleDto(TeamCompChamp tcc);

    // MAPPING REQUEST -> ENTITY
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teamCompChamps", ignore = true)
    @Mapping(target = "sets", ignore = true)
    @Mapping(target = "slug", expression = "java(generateSlug(request.getName()))")
    TeamComp toEntity(TeamCompRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teamCompChamps", ignore = true)
    @Mapping(target = "sets", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(TeamCompRequest request, @MappingTarget TeamComp entity);

    @AfterMapping
    default void fillSets(TeamCompRequest request, @MappingTarget TeamComp entity) {
        if (request.getSetId() != null) {
            Sets sets = new Sets();
            sets.setId(request.getSetId());
            entity.setSets(sets);
        }
    }

    default String generateSlug(String name) {
        if (name == null || name.isBlank()) return null;
        return name.toLowerCase().replaceAll("\\s+", "-");
    }
}
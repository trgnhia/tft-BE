package org.example.mapper;

import org.example.dto.champs.ChampResponse;
import org.example.dto.champs.ChampStatsRequest;
import org.example.dto.champs.CreateChampRequest;
import org.example.dto.champs.UpdateChampRequest;
import org.example.entities.champ.Champ;
import org.example.entities.champ.ChampStats;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ChampsMapper {

    @Mapping(target = "setsId", source = "sets.id")
    @Mapping(target = "setsName", source = "sets.name")
    ChampResponse toResponse(Champ champ);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sets", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "stats", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "champTraits", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Champ toEntity(CreateChampRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id",  ignore = true)
    @Mapping(target = "sets", ignore = true)
    @Mapping(target = "cost", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "champTraits", ignore = true)
    void updateEntity(UpdateChampRequest req, @MappingTarget Champ champ);

    ChampStats mapStats(ChampStatsRequest request);
}

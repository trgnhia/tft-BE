package org.example.mapper;

import org.example.dto.sets.SetsRequest;
import org.example.dto.sets.SetsResponse;
import org.example.entities.Sets;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SetsMapper {
    @Mapping(source = "createdByUser.username", target = "createdByName")
    SetsResponse toSetsResponse(Sets entity);

    List<SetsResponse> toListSetsResponse(List<Sets> listEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "code", ignore = true)
    Sets toEntity(SetsRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntity(SetsRequest request, @MappingTarget Sets entity);
}

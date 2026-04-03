package org.example.mapper;

import org.example.dto.sets.SetsResponse;
import org.example.entities.Sets;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SetsMapper {
    @Mapping(source = "createdByUser.username", target = "createdByName")
    SetsResponse toSetsResponse (Sets entity);


    // N+1 query
    List<SetsResponse> toListSetsResponse (List<Sets> listEntity);
}

package org.example.mapper;

import org.example.dto.sets.SetsResponse;
import org.example.entities.Sets;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SetsMapper {
    SetsResponse toSetsResponse (Sets entity);
    List<SetsResponse> toListSetsResponse (List<Sets> listEntity);
}

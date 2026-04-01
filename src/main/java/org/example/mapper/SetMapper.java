package org.example.mapper;

import org.example.dto.set.SetResponse;
import org.example.entities.Set;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SetMapper {
    SetResponse toSetResponse (Set entity);
    List<SetResponse> toListSetResponse (List<Set> listEntity);
}

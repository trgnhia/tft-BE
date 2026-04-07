package org.example.mapper;

import org.example.dto.trait.CreateTraitRequest;
import org.example.dto.trait.TraitBreakPointRequest;
import org.example.dto.trait.TraitResponse;
import org.example.dto.trait.UpdateTraitRequest;
import org.example.entities.trait.Trait;
import org.example.entities.trait.TraitBreakpoint;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TraitMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "sets", ignore = true)
    @Mapping(target = "champTraits", ignore = true)
    Trait toEntity(CreateTraitRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "sets", ignore = true)
    @Mapping(target = "champTraits", ignore = true)
    void updateEntity(UpdateTraitRequest request, @MappingTarget Trait trait);

    TraitResponse toResponse(Trait trait);
    TraitBreakpoint toBreakpointEntity(TraitBreakPointRequest request);
}
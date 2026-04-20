package org.example.mapper;

import org.example.dto.item.ItemRequest;
import org.example.dto.item.ItemResponse;
import org.example.entities.item.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(source = "sets.id", target = "setId")
    @Mapping(source = "sets.name", target = "setName")
    ItemResponse toItemResponse (Item entity);
    List<ItemResponse> toListItemResponse (List<Item> listItems);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sets", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Item toEntity(ItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sets", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(ItemRequest request, @MappingTarget Item entity);
}

package org.example.mapper;

import org.example.dto.champ_item_recommend.ChampItemRecommendRequest;
import org.example.dto.champ_item_recommend.ChampItemRecommendResponse;
import org.example.entities.ChampItemRecommend;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel =  "spring")
public interface ChampItemRecommendMapper {
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemName", source = "item.name")
    ChampItemRecommendResponse toResponse(ChampItemRecommend entity);

}

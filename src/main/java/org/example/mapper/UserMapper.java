package org.example.mapper;

import org.example.dto.user.UserResponse;
import org.example.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    @Mapping(target = "username", source = "username")
    @Mapping(target = "roleName", source = "role.code")
    @Mapping(target = "enabled", source = "enabled")
    UserResponse toResponse(User user);
}

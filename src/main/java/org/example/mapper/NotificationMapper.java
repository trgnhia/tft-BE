package org.example.mapper;

import org.example.dto.notification.NotificationCreateCommand;
import org.example.dto.notification.NotificationResponse;

import org.example.entities.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse (Notification entity);
    Notification toEntity (NotificationCreateCommand command);

}

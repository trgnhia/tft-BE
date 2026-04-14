package org.example.mapper;


import org.example.dto.chat.MessageResponse;
import org.example.entities.conversation.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "senderId", source = "sender.id")
    MessageResponse toResponse(Message message);

    List<MessageResponse> toResponseList(List<Message> messages);
}
package org.example.mapper;
import org.example.dto.chat.ChatMessageResponse;
import org.example.entities.conversation.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {
    @Mapping(target = "messageId", source = "message.id")
    @Mapping(target = "conversationId", source = "message.conversation.id")
    @Mapping(target = "senderId", source = "message.sender.id")
    @Mapping(target = "content", source = "message.content")
    @Mapping(target = "createdAt", source = "message.createdAt")
    @Mapping(target = "read", source = "message.read")
    ChatMessageResponse toResponse(Message message, Long receiverId);
}
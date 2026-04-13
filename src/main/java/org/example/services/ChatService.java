package org.example.services;

import org.example.dto.chat.ChatMessageResponse;
import org.example.dto.chat.ChatSendRequest;
import org.example.dto.chat.MessageResponse;
import org.example.dto.conversation.ConversationResponse;

import java.util.List;

public interface ChatService {
    ChatMessageResponse sendMessage(Long senderId, ChatSendRequest request);

    List<ConversationResponse> getMyConversations(Long currentUserId);

    List<MessageResponse> getMessagesByConversation(Long currentUserId, Long conversationId);

    Long getOrCreateConversation(Long currentUserId, Long otherUserId);
}

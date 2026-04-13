package org.example.services;

import org.example.dto.chat.ChatMessageResponse;
import org.example.dto.chat.ChatSendRequest;
import org.example.dto.chat.MessageResponse;
import org.example.dto.conversation.ConversationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {
    ChatMessageResponse sendMessage(Long senderId, ChatSendRequest request);

    List<ConversationResponse> getMyConversations(Long currentUserId);

    Page<MessageResponse> getMessagesByConversation(Long currentUserId, Long conversationId, Pageable pageable);

    Long getOrCreateConversation(Long currentUserId, Long otherUserId);
}

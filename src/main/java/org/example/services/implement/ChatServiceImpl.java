package org.example.services.implement;

import org.example.dto.chat.ChatMessageResponse;
import org.example.dto.chat.ChatSendRequest;
import org.example.dto.chat.MessageResponse;
import org.example.dto.conversation.ConversationResponse;
import org.example.services.ChatService;

import java.util.List;

public class ChatServiceImpl implements ChatService {
    @Override
    public ChatMessageResponse sendMessage(Long senderId, ChatSendRequest request) {
        return null;
    }

    @Override
    public List<ConversationResponse> getMyConversations(Long currentUserId) {
        return List.of();
    }

    @Override
    public List<MessageResponse> getMessagesByConversation(Long currentUserId, Long conversationId) {
        return List.of();
    }

    @Override
    public Long getOrCreateConversation(Long currentUserId, Long otherUserId) {
        return 0L;
    }
}

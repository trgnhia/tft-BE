package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.dto.chat.MessageResponse;
import org.example.dto.conversation.ConversationResponse;
import org.example.services.ChatService;
import org.example.util.SecurityUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatRestController {

    private final ChatService chatService;
    private final SecurityUtil securityUtil;

    @GetMapping("/conversations")
    public ApiResponse<List<ConversationResponse>> getMyConversations() {
        Long currentUserId = securityUtil.getCurrentUserIdOrThrow();
        return ApiResponse.success(chatService.getMyConversations(currentUserId));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ApiResponse<List<MessageResponse>> getMessagesByConversation(@PathVariable Long conversationId) {
        Long currentUserId = securityUtil.getCurrentUserIdOrThrow();
        return ApiResponse.success(chatService.getMessagesByConversation(currentUserId, conversationId));
    }
}
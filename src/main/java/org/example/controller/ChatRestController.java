package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.chat.MessageResponse;
import org.example.dto.conversation.ConversationResponse;
import org.example.services.ChatService;
import org.example.util.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

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
    public ApiResponse<PageResponse<MessageResponse>> getMessagesByConversation(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long currentUserId = securityUtil.getCurrentUserIdOrThrow();

        Pageable pageable = PageRequest.of(page, size);

        Page<MessageResponse> messagePage =
                chatService.getMessagesByConversation(currentUserId, conversationId, pageable);

        return ApiResponse.success(PageResponse.from(messagePage));
    }
}
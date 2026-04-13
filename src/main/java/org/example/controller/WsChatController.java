package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.chat.ChatMessageResponse;
import org.example.dto.chat.ChatSendRequest;
import org.example.services.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WsChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatSendRequest request, Principal principal) {
        Long senderId = Long.valueOf(principal.getName());
        ChatMessageResponse response = chatService.sendMessage(senderId, request);

        // gửi cho người nhận
        simpMessagingTemplate.convertAndSendToUser(
                String.valueOf(response.getReceiverId()),
                "/queue/messages",
                response
        );

        // echo lại cho người gửi để đồng bộ UI phía sender
        simpMessagingTemplate.convertAndSendToUser(
                String.valueOf(response.getSenderId()),
                "/queue/messages",
                response);
    }


}

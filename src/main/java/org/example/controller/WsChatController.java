package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.chat.ChatMessageResponse;
import org.example.dto.chat.ChatSendRequest;
import org.example.services.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor

public class WsChatController {
    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid @Payload ChatSendRequest request, Principal principal) {
        Long senderId = Long.valueOf(principal.getName());
        ChatMessageResponse response = chatService.sendMessage(senderId, request);

        // gửi cho người nhận
        simpMessagingTemplate.convertAndSendToUser(
                String.valueOf(response.getReceiverId()),
                "/queue/messages",
                response
        );

        log.info("Pushed to receiver done");
        // echo lại cho người gửi để đồng bộ UI phía sender
        simpMessagingTemplate.convertAndSendToUser(
                String.valueOf(response.getSenderId()),
                "/queue/messages",
                response);

        log.info("Pushed to sender done");
    }

}

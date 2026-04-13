package org.example.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChatSendRequest {
    @NotNull
    private Long receiverId;

    @NotBlank
    @Size(max = 4000)
    private String content;
}

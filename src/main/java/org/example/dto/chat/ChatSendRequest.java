package org.example.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatSendRequest {

    @NotNull(message = "{chat.receiver.id.not.null}")
    private Long receiverId;

    @NotBlank(message = "{chat.message.content.not.blank}")
    @Size(max = 4000, message = "{chat.message.content.max.size}")
    private String content;
}
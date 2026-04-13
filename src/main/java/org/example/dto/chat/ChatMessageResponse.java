package org.example.dto.chat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    private Long messageId;
    private Long conversationId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Instant createdAt;
    private boolean read;
}
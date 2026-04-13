package org.example.dto.conversation;

import lombok.Builder;

import java.time.Instant;

@Builder
public class ConversationParticipantResponse {
    private Long id;
    private Long conversationId;
    private Long userId;
    private Instant joinedAt;
}

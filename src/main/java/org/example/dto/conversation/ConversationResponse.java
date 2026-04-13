package org.example.dto.conversation;
import lombok.Builder;

import java.time.Instant;

@Builder
public class ConversationResponse{
    private Long id;
    private Instant createdAt;
    Instant updatedAt;
}
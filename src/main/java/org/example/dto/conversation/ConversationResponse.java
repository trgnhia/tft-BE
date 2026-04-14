package org.example.dto.conversation;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse{
    private Long id;
    private Instant createdAt;
    Instant updatedAt;
    private Long otherUserId;
    private String otherUsername;
    private String lastMessage;
    private Instant lastMessageAt;
}
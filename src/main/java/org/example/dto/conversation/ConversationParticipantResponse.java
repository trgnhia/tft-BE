package org.example.dto.conversation;

import lombok.*;

import java.time.Instant;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationParticipantResponse {
    private Long id;
    private Long conversationId;
    private Long userId;
    private Instant joinedAt;
}

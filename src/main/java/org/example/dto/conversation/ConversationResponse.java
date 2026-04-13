package org.example.dto.conversation;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse{
    private Long id;
    private Instant createdAt;
    Instant updatedAt;
}
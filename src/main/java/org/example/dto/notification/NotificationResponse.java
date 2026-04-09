package org.example.dto.notification;

import lombok.*;
import org.example.common.enums.NotificationTargetType;
import org.example.common.enums.NotificationType;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String title;
    private String content;
    private NotificationTargetType targetType;
    private Long targetId;
    private Long createdBy;
    private Instant createdAt;

}

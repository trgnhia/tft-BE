package org.example.dto.notification;

import lombok.*;
import org.example.common.enums.NotificationTargetType;
import org.example.common.enums.NotificationType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationCreateCommand {
    private NotificationType type;
    private String title;
    private String content;
    private NotificationTargetType targetType;
    private Long targetId;
    private Long createdBy;
}
package org.example.events.sets;

import lombok.RequiredArgsConstructor;
import org.example.common.enums.NotificationTargetType;
import org.example.common.enums.NotificationType;
import org.example.dto.notification.NotificationCreateCommand;
import org.example.services.NotificationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SetNotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSetChanged(SetChangedEvent event) {
        NotificationPayload payload = buildPayload(event.action());
        notificationService.createAndBroadcast(
                NotificationCreateCommand.builder()
                        .type(payload.type())
                        .title(payload.title())
                        .content("Set " + event.setName() + payload.contentSuffix())
                        .targetType(NotificationTargetType.SETS)
                        .targetId(event.setId())
                        .createdBy(event.createdBy())
                        .build()
        );
    }

    private NotificationPayload buildPayload(SetChangedEvent.Action action) {
        return switch (action) {
            case CREATED -> new NotificationPayload(
                    NotificationType.SET_CREATED,
                    "Tạo mùa giải mới",
                    " vừa được tạo"
            );
            case UPDATED -> new NotificationPayload(
                    NotificationType.SET_UPDATED,
                    "Cập nhật mùa giải",
                    " vừa được cập nhật"
            );
            case DELETED -> new NotificationPayload(
                    NotificationType.SET_DELETED,
                    "Xóa mùa giải",
                    " vừa bị xóa"
            );
        };
    }

    private record NotificationPayload(
            NotificationType type,
            String title,
            String contentSuffix
    ) {
    }
}

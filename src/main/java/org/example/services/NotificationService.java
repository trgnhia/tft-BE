package org.example.services;

import org.example.dto.notification.NotificationCreateCommand;
import org.example.dto.notification.NotificationResponse;

public interface NotificationService {
    NotificationResponse createAndBroadcast (
        NotificationCreateCommand notice
    );
}

package org.example.services;

import org.example.dto.notification.NotificationCreateCommand;
import org.example.dto.notification.NotificationResponse;

import java.util.List;

public interface NotificationService {
    NotificationResponse createAndBroadcast (
        NotificationCreateCommand notice
    );
    List<NotificationResponse> getAllNotifications ();
    List<NotificationResponse> getTop10Notifications ();
}

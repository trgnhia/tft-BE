package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import org.example.dto.notification.NotificationCreateCommand;
import org.example.dto.notification.NotificationResponse;
import org.example.entities.Notification;
import org.example.mapper.NotificationMapper;
import org.example.repositories.NotificationRepository;
import org.example.services.NotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository noticeRepo;
    private final NotificationMapper noticeMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public NotificationResponse createAndBroadcast(NotificationCreateCommand notice) {
        Notification notification = noticeMapper.toEntity(notice);
        Notification saved = noticeRepo.save(notification);
        NotificationResponse response = noticeMapper.toResponse(saved);
        messagingTemplate.convertAndSend(
                "/topic/cms-notifications",
                response
        );
        return response;
    }
}

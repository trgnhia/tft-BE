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

import java.util.List;

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
        //tất cả client subscribe /topic/cms-notifications sẽ nhận
        messagingTemplate.convertAndSend(
                "/topic/cms-notifications",
                response
        );
        return response;
    }
    @Override
    public List<NotificationResponse> getTop10Notifications() {
        return noticeRepo.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(noticeMapper::toResponse)
                .toList();
    }

    @Override
    public List<NotificationResponse> getAllNotifications() {
        return noticeRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(noticeMapper::toResponse)
                .toList();
    }
}

package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.dto.notification.NotificationCreateCommand;
import org.example.dto.notification.NotificationResponse;
import org.example.services.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<NotificationResponse>> create(
            @RequestBody NotificationCreateCommand request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(notificationService.createAndBroadcast(request))
        );
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getLatest() {
        return ResponseEntity.ok(
                ApiResponse.success(notificationService.getTop10Notifications())
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success(notificationService.getAllNotifications())
        );
    }
}
package com.securevault.backend.controller;

import com.securevault.backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.securevault.backend.dto.NotificationResponse;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                notificationService.getNotifications(email)
        );
    }

    @PutMapping("/{id}/read")
public ResponseEntity<?> markAsRead(
        @PathVariable Long id,
        Authentication authentication) {

    String email = authentication.getName();

    notificationService.markAsRead(id, email);

    return ResponseEntity.ok(
            java.util.Map.of(
                    "message", "Notification marked as read."
            )
    );
}

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(email)
        );
    }
}
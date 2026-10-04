package com.securevault.backend.service;

import com.securevault.backend.entity.Notification;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.NotificationRepository;
import com.securevault.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.securevault.backend.dto.NotificationResponse;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Notification createNotification(
            String email,
            String message) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    public List<NotificationResponse> getNotifications(String email) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found"));

    return notificationRepository
            .findByUserOrderByCreatedAtDesc(user)
            .stream()
            .map(notification -> new NotificationResponse(
                    notification.getId(),
                    notification.getMessage(),
                    notification.isRead(),
                    notification.getCreatedAt()
            ))
            .toList();
}

@Transactional
public boolean markAsRead(Long notificationId, String email) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found"));

    Notification notification = notificationRepository
            .findByIdAndUser(notificationId, user)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "Notification not found"
                    ));

    notification.setRead(true);
    notificationRepository.save(notification);

    return true;
}

   public List<NotificationResponse> getUnreadNotifications(String email) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new IllegalArgumentException("User not found"));

    return notificationRepository
            .findByUserAndReadFalseOrderByCreatedAtDesc(user)
            .stream()
            .map(notification -> new NotificationResponse(
                    notification.getId(),
                    notification.getMessage(),
                    notification.isRead(),
                    notification.getCreatedAt()
            ))
            .toList();
}
}
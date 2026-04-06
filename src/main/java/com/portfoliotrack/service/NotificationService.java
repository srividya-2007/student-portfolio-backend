package com.portfoliotrack.service;

import com.portfoliotrack.entity.Notification;
import com.portfoliotrack.entity.User;
import com.portfoliotrack.repository.NotificationRepository;
import com.portfoliotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void createNotification(Long userId, String message, Notification.NotificationType type) {
        User user = userRepository.findById(userId).orElseThrow();
        Notification n = Notification.builder()
                .message(message)
                .type(type)
                .user(user)
                .read(false)
                .build();
        notificationRepository.save(n);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Map<String, Long> getUnreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        return Map.of("count", count);
    }

    public Notification markAsRead(Long notifId, Long userId) {
        Notification n = notificationRepository.findById(notifId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!n.getUser().getId().equals(userId)) throw new RuntimeException("Unauthorized");
        n.setRead(true);
        return notificationRepository.save(n);
    }

    public void broadcastNotification(Long adminId, String message) {
        List<User> students = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.STUDENT && u.isActive())
                .toList();
        for (User s : students) {
            createNotification(s.getId(), message, Notification.NotificationType.INFO);
        }
    }
}

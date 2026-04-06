package com.portfoliotrack.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class NotificationDto {

    @Data
    public static class SendRequest {
        private Long userId; // null = broadcast to all students
        private String message;
        private String type;
    }

    @Data
    public static class NotificationResponse {
        private Long id;
        private Long userId;
        private String message;
        private Boolean isRead;
        private String type;
        private LocalDateTime createdAt;
    }
}

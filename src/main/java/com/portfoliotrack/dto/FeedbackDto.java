package com.portfoliotrack.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class FeedbackDto {
    @Data
    public static class FeedbackRequest {
        private String comment;
    }

    @Data
    public static class FeedbackResponse {
        private Long id;
        private String comment;
        private String adminName;
        private LocalDateTime createdAt;
    }
}

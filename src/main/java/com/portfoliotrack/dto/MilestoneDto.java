package com.portfoliotrack.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MilestoneDto {
    @Data
    public static class MilestoneRequest {
        private String title;
        private String description;
        private LocalDate dueDate;
    }

    @Data
    public static class MilestoneResponse {
        private Long id;
        private String title;
        private String description;
        private LocalDate dueDate;
        private boolean completed;
        private LocalDateTime createdAt;
    }
}

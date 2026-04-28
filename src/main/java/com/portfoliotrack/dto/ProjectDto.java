package com.portfoliotrack.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectDto {

    @Data
    public static class ProjectRequest {
        private String title;
        private String description;
        private String category;
        private String techStack;
        private String githubUrl;
        private String liveUrl;
        private String documentationUrl;
        private String showcaseLocation;
    }

    @Data
    public static class ProjectResponse {
        private Long id;
        private String title;
        private String description;
        private String category;
        private String techStack;
        private String githubUrl;
        private String liveUrl;
        private String documentationUrl;
        private String imageUrl;
        private String showcaseLocation;
        private String status;
        private String reviewComment;
        private Long studentUserId;
        private String studentName;
        private String studentId;
        private String department;
        private List<MilestoneDto.MilestoneResponse> milestones;
        private List<FeedbackDto.FeedbackResponse> feedbacks;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}

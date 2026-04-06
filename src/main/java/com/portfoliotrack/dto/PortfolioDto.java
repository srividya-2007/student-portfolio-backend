package com.portfoliotrack.dto;

import lombok.Data;

public class PortfolioDto {

    @Data
    public static class UpdateRequest {
        private String bio;
        private String skills;
        private String githubUrl;
        private String linkedinUrl;
        private String websiteUrl;
    }

    @Data
    public static class PortfolioResponse {
        private Long id;
        private Long userId;
        private String bio;
        private String skills;
        private String githubUrl;
        private String linkedinUrl;
        private String websiteUrl;
    }
}

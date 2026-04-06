package com.portfoliotrack.dto;

import lombok.Data;
import java.time.LocalDateTime;

public class UserDto {

    @Data
    public static class UpdateRequest {
        private String fullName;
        private String currentPassword;
        private String newPassword;
    }

    @Data
    public static class UserResponse {
        private Long id;
        private String name;        // mapped from User.fullName
        private String email;
        private String role;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }
}

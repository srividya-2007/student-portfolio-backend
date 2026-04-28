package com.portfoliotrack.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

public class AuthDto {

    @Data
    public static class RegisterRequest {
        @NotBlank private String fullName;
        @Email @NotBlank private String email;
        @Size(min = 6) @NotBlank private String password;
        private String role;
        private String studentId;
        private String department;
    }

    @Data
    public static class LoginRequest {
        @Email @NotBlank private String email;
        @NotBlank private String password;
    }

    @Data
    public static class AuthResponse {
        private Long id;
        private String fullName;
        private String email;
        private String studentId;
        private String department;
        private String role;
        private String token;
    }

    @Data
    public static class ForgotPasswordRequest {
        @Email @NotBlank private String email;
    }

    @Data
    public static class VerifyOtpRequest {
        @Email @NotBlank private String email;
        @Pattern(regexp = "\\d{6}", message = "OTP must be a 6-digit code")
        private String otp;
    }

    @Data
    public static class ResetPasswordRequest {
        @Email @NotBlank private String email;
        @Pattern(regexp = "\\d{6}", message = "OTP must be a 6-digit code")
        private String otp;
        @Size(min = 6) @NotBlank private String newPassword;
    }
}

package com.portfoliotrack.service;

import com.portfoliotrack.dto.AuthDto.*;
import com.portfoliotrack.entity.User;
import com.portfoliotrack.repository.UserRepository;
import com.portfoliotrack.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${app.auth.reset-otp-expiry-minutes:10}")
    private long resetOtpExpiryMinutes;

    public AuthResponse register(RegisterRequest req) {
        String email = normalizeEmail(req.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User.Role role = parseRole(req.getRole());
        if (role == User.Role.STUDENT) {
            if (isBlank(req.getStudentId()) || isBlank(req.getDepartment())) {
                throw new RuntimeException("Student ID and department are required for student registration");
            }
        }

        User user = User.builder()
                .fullName(req.getFullName())
                .email(email)
                .password(passwordEncoder.encode(req.getPassword()))
                .studentId(role == User.Role.STUDENT ? clean(req.getStudentId()) : null)
                .department(clean(req.getDepartment()))
                .role(role)
                .active(true)
                .build();
        userRepository.save(user);
        return buildResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(normalizeEmail(req.getEmail()))
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!user.isActive()) throw new RuntimeException("Account disabled");
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return buildResponse(user);
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new RuntimeException("Email not found"));
        String otp = generateOtp();
        user.setResetToken(otp);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(resetOtpExpiryMinutes));
        userRepository.save(user);
        emailService.sendPasswordResetOtpEmail(user.getEmail(), user.getFullName(), otp, resetOtpExpiryMinutes);
    }

    public void verifyResetOtp(VerifyOtpRequest req) {
        User user = userRepository.findByEmail(normalizeEmail(req.getEmail()))
                .orElseThrow(() -> new RuntimeException("Email not found"));
        validateResetOtp(user, req.getOtp());
    }

    public void resetPassword(ResetPasswordRequest req) {
        User user = userRepository.findByEmail(normalizeEmail(req.getEmail()))
                .orElseThrow(() -> new RuntimeException("Email not found"));
        validateResetOtp(user, req.getOtp());
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        AuthResponse res = new AuthResponse();
        res.setId(user.getId());
        res.setFullName(user.getFullName());
        res.setEmail(user.getEmail());
        res.setStudentId(user.getStudentId());
        res.setDepartment(user.getDepartment());
        res.setRole(user.getRole().name());
        res.setToken(token);
        return res;
    }

    private User.Role parseRole(String roleValue) {
        if (isBlank(roleValue)) {
            return User.Role.STUDENT;
        }

        try {
            return User.Role.valueOf(roleValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid role selected");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String clean(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String normalizeEmail(String email) {
        String cleanedEmail = clean(email);
        return cleanedEmail == null ? null : cleanedEmail.toLowerCase();
    }

    private void validateResetOtp(User user, String otp) {
        if (isBlank(user.getResetToken()) || user.getResetTokenExpiry() == null) {
            throw new RuntimeException("Request a new OTP to continue");
        }
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired. Request a new code");
        }
        if (!user.getResetToken().equals(clean(otp))) {
            throw new RuntimeException("Invalid OTP");
        }
    }

    private String generateOtp() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1_000_000));
    }
}

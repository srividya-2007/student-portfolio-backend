package com.portfoliotrack.controller;

import com.portfoliotrack.dto.FeedbackDto.*;
import com.portfoliotrack.dto.ProjectDto.ProjectResponse;
import com.portfoliotrack.entity.User;
import com.portfoliotrack.repository.ProjectRepository;
import com.portfoliotrack.repository.UserRepository;
import com.portfoliotrack.service.AdminService;
import com.portfoliotrack.service.NotificationService;
import com.portfoliotrack.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ProjectService projectService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @PutMapping("/projects/{id}/review")
    public ResponseEntity<ProjectResponse> reviewProject(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(projectService.reviewProject(id, status, comment, admin.getId()));
    }

    @PostMapping("/projects/{id}/feedback")
    public ResponseEntity<FeedbackResponse> addFeedback(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin,
            @RequestBody FeedbackRequest req) {
        return ResponseEntity.ok(projectService.addFeedback(id, admin.getId(), req));
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllStudents() {
        List<Map<String, Object>> students = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.STUDENT)
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", u.getId());
                    m.put("fullName", u.getFullName());
                    m.put("email", u.getEmail());
                    m.put("studentId", u.getStudentId());
                    m.put("department", u.getDepartment());
                    m.put("active", u.isActive());
                    m.put("createdAt", u.getCreatedAt());
                    m.put("projectCount", projectRepository.findByStudentId(u.getId()).size());
                    return m;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(students);
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("id", user.getId(), "active", user.isActive()));
    }

    @PostMapping("/notifications")
    public ResponseEntity<Map<String, String>> broadcastNotification(
            @AuthenticationPrincipal User admin,
            @RequestBody Map<String, String> body) {
        notificationService.broadcastNotification(admin.getId(), body.get("message"));
        return ResponseEntity.ok(Map.of("message", "Notification sent to all students"));
    }
}

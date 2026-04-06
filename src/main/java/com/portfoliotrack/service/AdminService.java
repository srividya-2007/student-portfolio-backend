package com.portfoliotrack.service;

import com.portfoliotrack.dto.FeedbackDto;
import com.portfoliotrack.dto.ProjectDto;
import com.portfoliotrack.dto.UserDto;
import com.portfoliotrack.entity.*;
import com.portfoliotrack.exception.ResourceNotFoundException;
import com.portfoliotrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final FeedbackRepository feedbackRepository;
    private final NotificationService notificationService;
    private final ProjectService projectService;
    private final UserService userService;

    public Map<String, Object> getDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalStudents", userRepository.findByRole(User.Role.STUDENT).size());
        data.put("totalProjects", projectRepository.count());
        data.put("pendingReviews", projectRepository.countByStatus(Project.ProjectStatus.PENDING));

        Map<String, Long> statusDist = new HashMap<>();
        statusDist.put("PENDING",      projectRepository.countByStatus(Project.ProjectStatus.PENDING));
        statusDist.put("UNDER_REVIEW", projectRepository.countByStatus(Project.ProjectStatus.UNDER_REVIEW));
        statusDist.put("APPROVED",     projectRepository.countByStatus(Project.ProjectStatus.APPROVED));
        statusDist.put("REJECTED",     projectRepository.countByStatus(Project.ProjectStatus.REJECTED));
        data.put("projectStatusDistribution", statusDist);

        List<ProjectDto.ProjectResponse> recent = projectRepository.findAllOrderByCreatedAtDesc().stream()
                .limit(5)
                .map(projectService::toResponse)
                .collect(Collectors.toList());
        data.put("recentProjects", recent);

        return data;
    }

    public ProjectDto.ProjectResponse reviewProject(Long projectId, String reviewStatus, String adminComment, Long adminId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        project.setStatus(Project.ProjectStatus.valueOf(reviewStatus));
        project.setReviewComment(adminComment);
        projectRepository.save(project);

        String msg = "Your project '" + project.getTitle() + "' has been " + reviewStatus.toLowerCase() + ".";
        if (adminComment != null && !adminComment.isBlank()) msg += " Comment: " + adminComment;
        notificationService.createNotification(
                project.getStudent().getId(),
                msg,
                "APPROVED".equals(reviewStatus) ? Notification.NotificationType.SUCCESS : Notification.NotificationType.WARNING
        );

        return projectService.toResponse(project);
    }

    public FeedbackDto.FeedbackResponse addFeedback(Long projectId, Long adminId, FeedbackDto.FeedbackRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        Feedback feedback = Feedback.builder()
                .project(project)
                .admin(admin)
                .comment(request.getComment())
                .build();
        Feedback saved = feedbackRepository.save(feedback);

        notificationService.createNotification(
                project.getStudent().getId(),
                "You received feedback on your project '" + project.getTitle() + "'",
                Notification.NotificationType.INFO
        );

        FeedbackDto.FeedbackResponse r = new FeedbackDto.FeedbackResponse();
        r.setId(saved.getId());
        r.setComment(saved.getComment());
        r.setAdminName(admin.getFullName());
        r.setCreatedAt(saved.getCreatedAt());
        return r;
    }

    public List<UserDto.UserResponse> getAllStudents() {
        return userService.getAllStudents();
    }

    public UserDto.UserResponse toggleUserStatus(Long userId) {
        return userService.toggleUserStatus(userId);
    }
}

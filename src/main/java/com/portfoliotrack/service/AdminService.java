package com.portfoliotrack.service;

import com.portfoliotrack.dto.FeedbackDto;
import com.portfoliotrack.dto.ProjectDto;
import com.portfoliotrack.dto.UserDto;
import com.portfoliotrack.entity.Feedback;
import com.portfoliotrack.entity.Notification;
import com.portfoliotrack.entity.Project;
import com.portfoliotrack.entity.User;
import com.portfoliotrack.exception.ResourceNotFoundException;
import com.portfoliotrack.repository.FeedbackRepository;
import com.portfoliotrack.repository.ProjectRepository;
import com.portfoliotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        List<User> students = userRepository.findByRole(User.Role.STUDENT);
        List<Project> allProjects = projectRepository.findAllOrderByCreatedAtDesc();

        long totalProjects = allProjects.size();
        long approvedCount = projectRepository.countByStatus(Project.ProjectStatus.APPROVED);
        long pendingCount = projectRepository.countByStatus(Project.ProjectStatus.PENDING);
        long rejectedCount = projectRepository.countByStatus(Project.ProjectStatus.REJECTED);
        long underReviewCount = projectRepository.countByStatus(Project.ProjectStatus.UNDER_REVIEW);

        Map<String, Object> data = new HashMap<>();
        data.put("totalStudents", students.size());
        data.put("totalProjects", totalProjects);
        data.put("approvedCount", approvedCount);
        data.put("pendingCount", pendingCount);
        data.put("rejectedCount", rejectedCount);
        data.put("underReviewCount", underReviewCount);
        data.put("pendingReviews", pendingCount + underReviewCount);
        data.put("approvalRate", totalProjects == 0 ? 0.0 : Math.round((approvedCount * 1000.0) / totalProjects) / 10.0);
        data.put("averageProjectsPerStudent",
                students.isEmpty() ? 0.0 : Math.round((totalProjects * 100.0) / students.size()) / 100.0);

        Map<String, Long> statusDist = new LinkedHashMap<>();
        statusDist.put("PENDING", pendingCount);
        statusDist.put("UNDER_REVIEW", underReviewCount);
        statusDist.put("APPROVED", approvedCount);
        statusDist.put("REJECTED", rejectedCount);
        data.put("projectStatusDistribution", statusDist);

        data.put("categoryDistribution", toNamedValueList(allProjects, Project::getCategory, "category"));
        data.put("departmentDistribution", toNamedValueList(allProjects,
                project -> project.getStudent() != null ? project.getStudent().getDepartment() : null,
                "department"));
        data.put("monthlySubmissions", buildMonthlySubmissions(allProjects, 6));
        data.put("topTechStack", buildTopTechStack(allProjects, 6));
        data.put("resourceCoverage", List.of(
                metricEntry("GitHub Linked", countProjects(allProjects, project -> hasText(project.getGithubUrl()))),
                metricEntry("Live Demo Added", countProjects(allProjects, project -> hasText(project.getLiveUrl()))),
                metricEntry("Docs Attached", countProjects(allProjects, project -> hasText(project.getDocumentationUrl()))),
                metricEntry("Map Location Added", countProjects(allProjects, project -> hasText(project.getShowcaseLocation())))
        ));

        List<ProjectDto.ProjectResponse> recent = allProjects.stream()
                .limit(6)
                .map(projectService::toResponse)
                .toList();
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
        if (adminComment != null && !adminComment.isBlank()) {
            msg += " Comment: " + adminComment;
        }
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

        FeedbackDto.FeedbackResponse response = new FeedbackDto.FeedbackResponse();
        response.setId(saved.getId());
        response.setComment(saved.getComment());
        response.setAdminName(admin.getFullName());
        response.setCreatedAt(saved.getCreatedAt());
        return response;
    }

    public List<UserDto.UserResponse> getAllStudents() {
        return userService.getAllStudents();
    }

    public UserDto.UserResponse toggleUserStatus(Long userId) {
        return userService.toggleUserStatus(userId);
    }

    private List<Map<String, Object>> toNamedValueList(
            List<Project> projects,
            Function<Project, String> classifier,
            String labelKey) {
        return projects.stream()
                .map(classifier)
                .filter(this::hasText)
                .map(String::trim)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> Map.<String, Object>of(labelKey, entry.getKey(), "count", entry.getValue()))
                .toList();
    }

    private List<Map<String, Object>> buildMonthlySubmissions(List<Project> projects, int months) {
        LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);
        return IntStream.rangeClosed(0, months - 1)
                .mapToObj(offset -> firstDayOfCurrentMonth.minusMonths(months - 1L - offset))
                .map(monthStart -> {
                    YearMonth yearMonth = YearMonth.from(monthStart);
                    long count = projects.stream()
                            .filter(project -> project.getCreatedAt() != null)
                            .filter(project -> YearMonth.from(project.getCreatedAt()).equals(yearMonth))
                            .count();

                    return Map.<String, Object>of(
                            "month", formatMonth(monthStart),
                            "count", count
                    );
                })
                .toList();
    }

    private List<Map<String, Object>> buildTopTechStack(List<Project> projects, int limit) {
        Map<String, Long> techCounts = projects.stream()
                .map(Project::getTechStack)
                .filter(this::hasText)
                .flatMap(stack -> Arrays.stream(stack.split(",")))
                .map(String::trim)
                .filter(this::hasText)
                .map(this::normalizeTech)
                .collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));

        return techCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> Map.<String, Object>of("tech", entry.getKey(), "count", entry.getValue()))
                .toList();
    }

    private Map<String, Object> metricEntry(String label, long value) {
        return Map.of("label", label, "value", value);
    }

    private long countProjects(List<Project> projects, Predicate<Project> predicate) {
        return projects.stream()
                .filter(predicate)
                .count();
    }

    private String formatMonth(LocalDate monthStart) {
        String monthName = monthStart.getMonth().name().toLowerCase();
        return monthName.substring(0, 1).toUpperCase() + monthName.substring(1, 3);
    }

    private String normalizeTech(String tech) {
        String trimmedTech = tech.trim();
        if (trimmedTech.isEmpty()) {
            return trimmedTech;
        }
        return trimmedTech.substring(0, 1).toUpperCase() + trimmedTech.substring(1);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

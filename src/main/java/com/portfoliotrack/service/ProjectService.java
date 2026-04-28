package com.portfoliotrack.service;

import com.portfoliotrack.dto.ProjectDto.ProjectRequest;
import com.portfoliotrack.dto.ProjectDto.ProjectResponse;
import com.portfoliotrack.dto.MilestoneDto.MilestoneRequest;
import com.portfoliotrack.dto.MilestoneDto.MilestoneResponse;
import com.portfoliotrack.dto.FeedbackDto.FeedbackRequest;
import com.portfoliotrack.dto.FeedbackDto.FeedbackResponse;
import com.portfoliotrack.entity.*;
import com.portfoliotrack.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final MilestoneRepository milestoneRepository;
    private final FeedbackRepository feedbackRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAllOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getMyProjects(Long userId) {
        return projectRepository.findByStudentId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getStudentProjects(Long userId) {
        return projectRepository.findByStudentId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long id) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return toResponse(p);
    }

    @Transactional
    public ProjectResponse createProject(Long userId, ProjectRequest req) {
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Project p = Project.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .category(req.getCategory())
                .techStack(req.getTechStack())
                .githubUrl(req.getGithubUrl())
                .liveUrl(req.getLiveUrl())
                .documentationUrl(req.getDocumentationUrl())
                .showcaseLocation(req.getShowcaseLocation())
                .status(Project.ProjectStatus.PENDING)
                .student(student)
                .build();
        projectRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, Long userId, ProjectRequest req) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!p.getStudent().getId().equals(userId))
            throw new RuntimeException("Unauthorized");
        p.setTitle(req.getTitle());
        p.setDescription(req.getDescription());
        p.setCategory(req.getCategory());
        p.setTechStack(req.getTechStack());
        p.setGithubUrl(req.getGithubUrl());
        p.setLiveUrl(req.getLiveUrl());
        p.setDocumentationUrl(req.getDocumentationUrl());
        p.setShowcaseLocation(req.getShowcaseLocation());
        p.setStatus(Project.ProjectStatus.PENDING);
        projectRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public void deleteProject(Long id, Long userId) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!p.getStudent().getId().equals(userId))
            throw new RuntimeException("Unauthorized");
        projectRepository.delete(p);
    }

    @Transactional
    public ProjectResponse reviewProject(Long id, String status, String comment, Long adminId) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        p.setStatus(Project.ProjectStatus.valueOf(status));
        p.setReviewComment(comment);
        projectRepository.save(p);

        String msg = "Your project '" + p.getTitle() + "' has been " + status.toLowerCase().replace("_", " ");
        notificationService.createNotification(p.getStudent().getId(), msg,
                status.equals("APPROVED") ? Notification.NotificationType.SUCCESS : Notification.NotificationType.WARNING);
        return toResponse(p);
    }

    @Transactional
    public MilestoneResponse addMilestone(Long projectId, Long userId, MilestoneRequest req) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!p.getStudent().getId().equals(userId))
            throw new RuntimeException("Unauthorized");
        Milestone m = Milestone.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .dueDate(req.getDueDate())
                .project(p)
                .build();
        milestoneRepository.save(m);
        return toMilestoneResponse(m);
    }

    @Transactional
    public MilestoneResponse updateMilestone(Long projectId, Long milestoneId, Long userId, MilestoneRequest req) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!p.getStudent().getId().equals(userId))
            throw new RuntimeException("Unauthorized");
        Milestone m = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));
        if (req.getTitle() != null && !req.getTitle().isBlank()) m.setTitle(req.getTitle());
        if (req.getDescription() != null) m.setDescription(req.getDescription());
        if (req.getDueDate() != null) m.setDueDate(req.getDueDate());
        milestoneRepository.save(m);
        return toMilestoneResponse(m);
    }

    @Transactional
    public FeedbackResponse addFeedback(Long projectId, Long adminId, FeedbackRequest req) {
        Project p = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        Feedback fb = Feedback.builder()
                .comment(req.getComment())
                .project(p)
                .admin(admin)
                .build();
        feedbackRepository.save(fb);

        notificationService.createNotification(p.getStudent().getId(),
                "You received feedback on your project '" + p.getTitle() + "'",
                Notification.NotificationType.INFO);
        return toFeedbackResponse(fb);
    }

    public ProjectResponse toResponse(Project p) {
        ProjectResponse r = new ProjectResponse();
        r.setId(p.getId());
        r.setTitle(p.getTitle());
        r.setDescription(p.getDescription());
        r.setCategory(p.getCategory());
        r.setTechStack(p.getTechStack());
        r.setGithubUrl(p.getGithubUrl());
        r.setLiveUrl(p.getLiveUrl());
        r.setDocumentationUrl(p.getDocumentationUrl());
        r.setImageUrl(p.getImageUrl());
        r.setShowcaseLocation(p.getShowcaseLocation());
        r.setStatus(p.getStatus().name());
        r.setReviewComment(p.getReviewComment());
        r.setStudentUserId(p.getStudent().getId());
        r.setStudentName(p.getStudent().getFullName());
        r.setStudentId(p.getStudent().getStudentId());
        r.setDepartment(p.getStudent().getDepartment());
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());
        if (p.getMilestones() != null)
            r.setMilestones(p.getMilestones().stream().map(this::toMilestoneResponse).collect(Collectors.toList()));
        if (p.getFeedbacks() != null)
            r.setFeedbacks(p.getFeedbacks().stream().map(this::toFeedbackResponse).collect(Collectors.toList()));
        return r;
    }

    private MilestoneResponse toMilestoneResponse(Milestone m) {
        MilestoneResponse r = new MilestoneResponse();
        r.setId(m.getId());
        r.setTitle(m.getTitle());
        r.setDescription(m.getDescription());
        r.setDueDate(m.getDueDate());
        r.setCompleted(m.isCompleted());
        r.setCreatedAt(m.getCreatedAt());
        return r;
    }

    private FeedbackResponse toFeedbackResponse(Feedback f) {
        FeedbackResponse r = new FeedbackResponse();
        r.setId(f.getId());
        r.setComment(f.getComment());
        r.setAdminName(f.getAdmin().getFullName());
        r.setCreatedAt(f.getCreatedAt());
        return r;
    }
}

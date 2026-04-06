package com.portfoliotrack.controller;

import com.portfoliotrack.dto.ProjectDto.ProjectRequest;
import com.portfoliotrack.dto.ProjectDto.ProjectResponse;
import com.portfoliotrack.dto.MilestoneDto.MilestoneRequest;
import com.portfoliotrack.dto.MilestoneDto.MilestoneResponse;
import com.portfoliotrack.entity.User;
import com.portfoliotrack.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAll() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProjectResponse>> getMyProjects(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(projectService.getMyProjects(user.getId()));
    }

    @GetMapping("/student/{userId}")
    public ResponseEntity<List<ProjectResponse>> getStudentProjects(@PathVariable Long userId) {
        return ResponseEntity.ok(projectService.getStudentProjects(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProject(id));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @AuthenticationPrincipal User user,
            @RequestBody ProjectRequest req) {
        return ResponseEntity.ok(projectService.createProject(user.getId(), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestBody ProjectRequest req) {
        return ResponseEntity.ok(projectService.updateProject(id, user.getId(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        projectService.deleteProject(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/milestones")
    public ResponseEntity<MilestoneResponse> addMilestone(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User user,
            @RequestBody MilestoneRequest req) {
        return ResponseEntity.ok(projectService.addMilestone(projectId, user.getId(), req));
    }

    @PutMapping("/{projectId}/milestones/{milestoneId}")
    public ResponseEntity<MilestoneResponse> updateMilestone(
            @PathVariable Long projectId,
            @PathVariable Long milestoneId,
            @AuthenticationPrincipal User user,
            @RequestBody MilestoneRequest req) {
        return ResponseEntity.ok(projectService.updateMilestone(projectId, milestoneId, user.getId(), req));
    }
}

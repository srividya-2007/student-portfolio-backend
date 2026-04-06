package com.portfoliotrack.controller;

import com.portfoliotrack.entity.User;
import com.portfoliotrack.entity.Portfolio;
import com.portfoliotrack.service.StudentService;
import com.portfoliotrack.service.StudentService.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getStudent(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudent(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateStudent(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @RequestBody StudentUpdateRequest req) {
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(studentService.updateStudent(id, req));
    }

    @GetMapping("/{id}/portfolio")
    public ResponseEntity<Portfolio> getPortfolio(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getPortfolio(id));
    }

    @PutMapping("/{id}/portfolio")
    public ResponseEntity<Portfolio> updatePortfolio(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser,
            @RequestBody PortfolioUpdateRequest req) {
        if (!currentUser.getId().equals(id)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(studentService.updatePortfolio(id, req));
    }
}

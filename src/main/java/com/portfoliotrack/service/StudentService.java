package com.portfoliotrack.service;

import com.portfoliotrack.entity.Portfolio;
import com.portfoliotrack.entity.User;
import com.portfoliotrack.repository.PortfolioRepository;
import com.portfoliotrack.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    public User getStudent(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    public User updateStudent(Long id, StudentUpdateRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        user.setFullName(req.getFullName());
        user.setStudentId(req.getStudentId());
        user.setDepartment(req.getDepartment());
        return userRepository.save(user);
    }

    public Portfolio getPortfolio(Long userId) {
        return portfolioRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId).orElseThrow();
            Portfolio pf = Portfolio.builder().user(user).build();
            return portfolioRepository.save(pf);
        });
    }

    public Portfolio updatePortfolio(Long userId, PortfolioUpdateRequest req) {
        Portfolio pf = portfolioRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId).orElseThrow();
            return Portfolio.builder().user(user).build();
        });
        pf.setBio(req.getBio());
        pf.setSkills(req.getSkills());
        pf.setGithubUrl(req.getGithubUrl());
        pf.setLinkedinUrl(req.getLinkedinUrl());
        pf.setWebsiteUrl(req.getWebsiteUrl());
        return portfolioRepository.save(pf);
    }

    @Data
    public static class StudentUpdateRequest {
        private String fullName;
        private String studentId;
        private String department;
    }

    @Data
    public static class PortfolioUpdateRequest {
        private String bio;
        private String skills;
        private String githubUrl;
        private String linkedinUrl;
        private String websiteUrl;
    }
}

package com.portfoliotrack.service;

import com.portfoliotrack.dto.UserDto;
import com.portfoliotrack.dto.PortfolioDto;
import com.portfoliotrack.entity.Portfolio;
import com.portfoliotrack.entity.User;
import com.portfoliotrack.exception.BadRequestException;
import com.portfoliotrack.exception.ResourceNotFoundException;
import com.portfoliotrack.repository.PortfolioRepository;
import com.portfoliotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDto.UserResponse getUserById(Long id) {
        return toResponse(findUser(id));
    }

    public UserDto.UserResponse updateUser(Long id, UserDto.UpdateRequest request) {
        User user = findUser(id);
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new BadRequestException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }
        return toResponse(userRepository.save(user));
    }

    public PortfolioDto.PortfolioResponse getPortfolio(Long userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found"));
        return toPortfolioResponse(portfolio);
    }

    public PortfolioDto.PortfolioResponse updatePortfolio(Long userId, PortfolioDto.UpdateRequest request) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = findUser(userId);
                    return Portfolio.builder().user(user).build();
                });
        if (request.getBio() != null) portfolio.setBio(request.getBio());
        if (request.getSkills() != null) portfolio.setSkills(request.getSkills());
        if (request.getGithubUrl() != null) portfolio.setGithubUrl(request.getGithubUrl());
        if (request.getLinkedinUrl() != null) portfolio.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getWebsiteUrl() != null) portfolio.setWebsiteUrl(request.getWebsiteUrl());
        return toPortfolioResponse(portfolioRepository.save(portfolio));
    }

    public List<UserDto.UserResponse> getAllStudents() {
        return userRepository.findByRole(User.Role.STUDENT)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UserDto.UserResponse toggleUserStatus(Long id) {
        User user = findUser(id);
        user.setActive(!user.isActive());
        return toResponse(userRepository.save(user));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserDto.UserResponse toResponse(User u) {
        UserDto.UserResponse r = new UserDto.UserResponse();
        r.setId(u.getId());
        r.setName(u.getFullName());
        r.setEmail(u.getEmail());
        r.setRole(u.getRole().name());
        r.setIsActive(u.isActive());
        r.setCreatedAt(u.getCreatedAt());
        return r;
    }

    private PortfolioDto.PortfolioResponse toPortfolioResponse(Portfolio p) {
        PortfolioDto.PortfolioResponse r = new PortfolioDto.PortfolioResponse();
        r.setId(p.getId());
        r.setUserId(p.getUser().getId());
        r.setBio(p.getBio());
        r.setSkills(p.getSkills());
        r.setGithubUrl(p.getGithubUrl());
        r.setLinkedinUrl(p.getLinkedinUrl());
        r.setWebsiteUrl(p.getWebsiteUrl());
        return r;
    }
}

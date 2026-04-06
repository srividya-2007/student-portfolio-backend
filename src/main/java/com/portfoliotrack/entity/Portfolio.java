package com.portfoliotrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolios")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Portfolio {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String skills;
    private String githubUrl;
    private String linkedinUrl;
    private String websiteUrl;
    private String avatarUrl;

    /** Prevent circular serialization: Portfolio → User → portfolio → Portfolio → … */
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

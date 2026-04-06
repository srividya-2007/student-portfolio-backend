package com.portfoliotrack.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Prevent circular serialization: ProjectMedia → Project → media → ProjectMedia → … */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    private FileType fileType;

    private String originalName;

    public enum FileType {
        IMAGE, VIDEO, PDF
    }
}

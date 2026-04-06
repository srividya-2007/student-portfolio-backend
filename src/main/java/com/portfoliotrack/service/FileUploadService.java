package com.portfoliotrack.service;

import com.portfoliotrack.entity.ProjectMedia;
import com.portfoliotrack.entity.Project;
import com.portfoliotrack.exception.BadRequestException;
import com.portfoliotrack.exception.ResourceNotFoundException;
import com.portfoliotrack.repository.ProjectMediaRepository;
import com.portfoliotrack.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private final ProjectRepository projectRepository;
    private final ProjectMediaRepository projectMediaRepository;

    public String uploadImage(MultipartFile file) {
        validateFile(file, new String[]{"image/jpeg", "image/png", "image/gif", "image/webp"}, 10);
        return saveFile(file, "images");
    }

    public String uploadFile(MultipartFile file) {
        validateFile(file, new String[]{"application/pdf", "video/mp4", "video/mpeg", "video/webm"}, 50);
        return saveFile(file, "files");
    }

    public ProjectMedia attachToProject(Long projectId, MultipartFile file, String fileType) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        String url;
        ProjectMedia.FileType type;
        switch (fileType.toUpperCase()) {
            case "IMAGE" -> { url = uploadImage(file); type = ProjectMedia.FileType.IMAGE; }
            case "VIDEO" -> { url = uploadFile(file); type = ProjectMedia.FileType.VIDEO; }
            case "PDF"   -> { url = uploadFile(file); type = ProjectMedia.FileType.PDF; }
            default -> throw new BadRequestException("Invalid file type: " + fileType);
        }

        ProjectMedia media = ProjectMedia.builder()
                .project(project)
                .fileUrl(url)
                .fileType(type)
                .originalName(file.getOriginalFilename())
                .build();
        return projectMediaRepository.save(media);
    }

    private String saveFile(MultipartFile file, String subDir) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename()
                    .replaceAll("[^a-zA-Z0-9._-]", "_");
            Path dir = Paths.get(uploadDir, subDir);
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + subDir + "/" + filename;
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file, String[] allowedTypes, long maxSizeMb) {
        if (file.isEmpty()) throw new BadRequestException("File is empty");
        String contentType = file.getContentType();
        boolean valid = false;
        for (String t : allowedTypes) { if (t.equals(contentType)) { valid = true; break; } }
        if (!valid) throw new BadRequestException("Invalid file type: " + contentType);
        if (file.getSize() > maxSizeMb * 1024 * 1024)
            throw new BadRequestException("File size exceeds " + maxSizeMb + "MB limit");
    }
}

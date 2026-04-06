package com.portfoliotrack.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(saveFile(file, "images"));
    }

    @PostMapping("/file")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(saveFile(file, "files"));
    }

    @PostMapping("/project/{projectId}")
    public ResponseEntity<Map<String, String>> uploadProjectMedia(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(saveFile(file, "projects/" + projectId));
    }

    private Map<String, String> saveFile(MultipartFile file, String subDir) throws IOException {
        String ext = "";
        String orig = file.getOriginalFilename();
        if (orig != null && orig.contains(".")) ext = orig.substring(orig.lastIndexOf('.'));
        String filename = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir, subDir);
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        String url = "/uploads/" + subDir + "/" + filename;
        return Map.of("url", url, "filename", filename);
    }
}

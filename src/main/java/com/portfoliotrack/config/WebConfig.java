package com.portfoliotrack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Serves files written to the local upload directory (default: ./uploads)
 * via the /uploads/** HTTP path.
 *
 * SecurityConfig already permits /uploads/**, but without this resource handler
 * Spring Boot would return 404 for every uploaded image/document.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadDir).toAbsolutePath().toString();
        // Ensure the location ends with a slash (required by Spring)
        String resourceLocation = "file:" + absolutePath + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}


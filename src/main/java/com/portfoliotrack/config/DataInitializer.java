package com.portfoliotrack.config;

import com.portfoliotrack.entity.User;
import com.portfoliotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with demo accounts on first startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedDemoStudent();
    }

    private void seedAdmin() {
        String adminEmail = "admin@portfoliotrack.com";
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin account already exists - skipping seed.");
            return;
        }

        User admin = User.builder()
                .fullName("System Administrator")
                .email(adminEmail)
                .password(passwordEncoder.encode("Admin@123"))
                .role(User.Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(admin);
        log.info("Default admin account created: {} / Admin@123", adminEmail);
    }

    private void seedDemoStudent() {
        String studentEmail = "student@portfoliotrack.com";
        if (userRepository.existsByEmail(studentEmail)) {
            log.info("Demo student account already exists - skipping seed.");
            return;
        }

        User student = User.builder()
                .fullName("Demo Student")
                .email(studentEmail)
                .password(passwordEncoder.encode("student123"))
                .studentId("22BCE0001")
                .department("Computer Science & Engineering")
                .role(User.Role.STUDENT)
                .active(true)
                .build();

        userRepository.save(student);
        log.info("Demo student account created: {} / student123", studentEmail);
    }
}

package com.portfoliotrack.config;

import com.portfoliotrack.entity.User;
import com.portfoliotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// @Component  ← disabled: DataInitializer.java is the sole admin seeder
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed default admin if none exists
        if (userRepository.findByEmail("admin@portfoliotrack.com").isEmpty()) {
            User admin = User.builder()
                    .fullName("System Administrator")
                    .email("admin@portfoliotrack.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info("✅ Default admin created: admin@portfoliotrack.com / admin123");
        }

        // Seed demo student
        if (userRepository.findByEmail("student@portfoliotrack.com").isEmpty()) {
            User student = User.builder()
                    .fullName("Demo Student")
                    .email("student@portfoliotrack.com")
                    .password(passwordEncoder.encode("student123"))
                    .studentId("22BCE0001")
                    .department("Computer Science & Engineering")
                    .role(User.Role.STUDENT)
                    .active(true)
                    .build();
            userRepository.save(student);
            log.info("✅ Demo student created: student@portfoliotrack.com / student123");
        }
    }
}

package com.portfoliotrack.config;

import com.portfoliotrack.entity.User;
import com.portfoliotrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with a default ADMIN user on first startup.
 * This is the only way to get an ADMIN account since the /api/auth/register
 * endpoint always creates STUDENT users.
 *
 * Default credentials:
 *   Email   : admin@portfoliotrack.com
 *   Password: Admin@123
 *
 * Change these immediately after first login.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@portfoliotrack.com";

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin account already exists — skipping seed.");
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
        log.info("======================================================");
        log.info(" Default admin account created:");
        log.info("   Email   : {}", adminEmail);
        log.info("   Password: Admin@123");
        log.info(" CHANGE THIS PASSWORD AFTER FIRST LOGIN!");
        log.info("======================================================");
    }
}

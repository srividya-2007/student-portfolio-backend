package com.portfoliotrack.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Wraps JavaMailSender with an optional dependency so the application
 * starts cleanly even when no SMTP server is configured locally.
 */
@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendPasswordResetOtpEmail(String to, String fullName, String otp, long expiryMinutes) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("PortfolioTrack - Password Reset Verification Code");
        msg.setText(
                "Hello " + (fullName == null || fullName.isBlank() ? "there" : fullName) + ",\n\n" +
                "Use this OTP to reset your PortfolioTrack password: " + otp + "\n\n" +
                "This code expires in " + expiryMinutes + " minutes.\n\n" +
                "If you did not request a password reset, you can safely ignore this email."
        );

        if (mailSender == null) {
            log.info("SMTP not configured. Password reset OTP for {} is {}", to, otp);
            return;
        }

        sendSafely(msg);
    }

    public void sendNotificationEmail(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("PortfolioTrack - " + subject);
        msg.setText(body);
        sendSafely(msg);
    }

    private void sendSafely(SimpleMailMessage msg) {
        try {
            mailSender.send(msg);
            log.debug("Email sent to {}", (Object) msg.getTo());
        } catch (MailException ex) {
            log.warn("Failed to send email to {} - check SMTP settings. Error: {}",
                    msg.getTo(), ex.getMessage());
        }
    }
}

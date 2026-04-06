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
 * Email operations are silently skipped when mailSender is null.
 */
@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String token) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("PortfolioTrack – Password Reset");
        msg.setText(
            "Click the link to reset your password:\n\n" +
            "http://localhost:5173/reset-password?token=" + token +
            "\n\nThis link expires in 1 hour."
        );
        sendSafely(msg);
    }

    public void sendNotificationEmail(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("PortfolioTrack – " + subject);
        msg.setText(body);
        sendSafely(msg);
    }

    /**
     * Sends the message and silently logs any SMTP errors.
     * Also skips silently when no JavaMailSender bean is configured.
     */
    private void sendSafely(SimpleMailMessage msg) {
        if (mailSender == null) {
            log.info("Email skipped (no SMTP configured) — recipient: {}", (Object) msg.getTo());
            return;
        }
        try {
            mailSender.send(msg);
            log.debug("Email sent to {}", (Object) msg.getTo());
        } catch (MailException ex) {
            log.warn("Failed to send email to {} — check SMTP settings. Error: {}",
                    msg.getTo(), ex.getMessage());
        }
    }
}

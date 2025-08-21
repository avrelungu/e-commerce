package com.example.notification_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class EmailService {

    private final Random random = new Random();

    public boolean sendEmail(String to, String subject, String message) {
        try {
            // Mock email sending - simulate network delay
            Thread.sleep(500 + random.nextInt(1000)); // 0.5-1.5 seconds delay

            // Simulate occasional failures (10% failure rate)
            if (random.nextDouble() < 0.1) {
                log.error("Failed to send email to: {} with subject: {}", to, subject);
                return false;
            }

            // Mock successful email sending
            log.info("✉️ EMAIL SENT to: {} | Subject: {} | Message: {}", 
                    to, subject, truncateMessage(message));
            
            return true;
            
        } catch (Exception e) {
            log.error("Error sending email to: {}", to, e);
            return false;
        }
    }

    public boolean sendEmailWithRetry(String to, String subject, String message, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.info("Email attempt {} of {} to: {}", attempt, maxRetries, to);
            
            if (sendEmail(to, subject, message)) {
                return true;
            }
            
            if (attempt < maxRetries) {
                try {
                    // Exponential backoff: 2^attempt seconds
                    Thread.sleep((long) Math.pow(2, attempt) * 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        log.error("Failed to send email after {} attempts to: {}", maxRetries, to);
        return false;
    }

    private String truncateMessage(String message) {
        if (message.length() <= 100) {
            return message;
        }
        return message.substring(0, 100) + "...";
    }
}
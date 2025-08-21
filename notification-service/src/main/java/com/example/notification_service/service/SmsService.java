package com.example.notification_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class SmsService {

    private final Random random = new Random();

    public boolean sendSms(String phoneNumber, String message) {
        try {
            // Mock SMS sending - simulate network delay
            Thread.sleep(300 + random.nextInt(700)); // 0.3-1.0 seconds delay

            // Simulate occasional failures (5% failure rate)
            if (random.nextDouble() < 0.05) {
                log.error("Failed to send SMS to: {}", phoneNumber);
                return false;
            }

            // Mock successful SMS sending
            log.info("📱 SMS SENT to: {} | Message: {}", 
                    maskPhoneNumber(phoneNumber), truncateMessage(message));
            
            return true;
            
        } catch (Exception e) {
            log.error("Error sending SMS to: {}", phoneNumber, e);
            return false;
        }
    }

    public boolean sendSmsWithRetry(String phoneNumber, String message, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.info("SMS attempt {} of {} to: {}", attempt, maxRetries, maskPhoneNumber(phoneNumber));
            
            if (sendSms(phoneNumber, message)) {
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
        
        log.error("Failed to send SMS after {} attempts to: {}", maxRetries, maskPhoneNumber(phoneNumber));
        return false;
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return "***-***-" + phoneNumber.substring(phoneNumber.length() - 4);
    }

    private String truncateMessage(String message) {
        if (message.length() <= 160) { // SMS character limit
            return message;
        }
        return message.substring(0, 157) + "...";
    }
}
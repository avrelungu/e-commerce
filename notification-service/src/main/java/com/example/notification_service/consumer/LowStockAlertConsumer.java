package com.example.notification_service.consumer;

import com.example.events.inventory.LowStockAlertEvent;
import com.example.notification_service.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class LowStockAlertConsumer {

    private final SmsService smsService;
    
    @Value("${notification.managers.phone-numbers:+1-555-1001,+1-555-1002,+1-555-1003}")
    private String managersPhoneNumbers;

    public LowStockAlertConsumer(SmsService smsService) {
        this.smsService = smsService;
    }

    @KafkaListener(topics = "#{kafkaTopics.lowStockAlert}", groupId = "notification-service-low-stock")
    public void handleLowStockAlert(LowStockAlertEvent lowStockAlertEvent) {
        try {
            log.info("Received low stock alert for product: {} (SKU: {}), quantity: {}, threshold: {}", 
                lowStockAlertEvent.getProductName(), 
                lowStockAlertEvent.getSku(),
                lowStockAlertEvent.getCurrentQuantity(),
                lowStockAlertEvent.getThreshold());

            String smsMessage = String.format(
                "🚨 LOW STOCK ALERT\n" +
                "Product: %s\n" +
                "SKU: %s\n" +
                "Current: %d units\n" +
                "Threshold: %d units\n" +
                "Action required: Reorder immediately",
                lowStockAlertEvent.getProductName(),
                lowStockAlertEvent.getSku(),
                lowStockAlertEvent.getCurrentQuantity(),
                lowStockAlertEvent.getThreshold()
            );

            String[] phoneNumbers = managersPhoneNumbers.split(",");
            
            for (String phoneNumber : phoneNumbers) {
                phoneNumber = phoneNumber.trim();
                boolean sent = smsService.sendSmsWithRetry(phoneNumber, smsMessage, 3);
                
                if (sent) {
                    log.info("Low stock SMS alert sent to manager: {}", phoneNumber);
                } else {
                    log.error("Failed to send low stock SMS alert to manager: {}", phoneNumber);
                }
            }

            log.info("Low stock alert processing completed for product: {}", lowStockAlertEvent.getSku());

        } catch (Exception e) {
            log.error("Error processing low stock alert for product: {}", 
                lowStockAlertEvent != null ? lowStockAlertEvent.getSku() : "unknown", e);
        }
    }
}
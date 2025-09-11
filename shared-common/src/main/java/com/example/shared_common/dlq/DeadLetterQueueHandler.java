package com.example.shared_common.dlq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DeadLetterQueueHandler {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String DLQ_TOPIC = "dead-letter-queue";
    
    public DeadLetterQueueHandler(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }
    
    public void handleFailedMessage(
            Object message,
            String originalTopic,
            String errorMessage,
            Exception exception) {
        
        log.error("Message sent to DLQ from topic: {}, error: {}", originalTopic, errorMessage);
        
        try {
            // Create DLQ message with metadata
            Map<String, Object> dlqMessage = new HashMap<>();
            dlqMessage.put("originalTopic", originalTopic);
            dlqMessage.put("failureReason", errorMessage);
            dlqMessage.put("exception", exception != null ? exception.getClass().getSimpleName() : "Unknown");
            dlqMessage.put("stackTrace", exception != null ? getStackTrace(exception) : "");
            dlqMessage.put("originalMessage", message);
            dlqMessage.put("timestamp", LocalDateTime.now().toString());
            dlqMessage.put("retryCount", 0);
            dlqMessage.put("serviceName", getServiceName());
            
            // Send to DLQ topic
            kafkaTemplate.send(DLQ_TOPIC, dlqMessage);
            
            log.info("Failed message stored in DLQ for manual processing: topic={}, service={}", 
                originalTopic, getServiceName());
            
        } catch (Exception e) {
            log.error("Failed to send message to DLQ - this is critical!", e);
            // In production, you might want to store this in a database or file system
            storeCriticalFailure(message, originalTopic, errorMessage, e);
        }
    }
    
    public void handleRetryableFailure(
            Object message,
            String originalTopic,
            String errorMessage,
            int retryCount,
            int maxRetries) {
        
        if (retryCount >= maxRetries) {
            log.warn("Max retries ({}) reached for message from topic: {}", maxRetries, originalTopic);
            handleFailedMessage(message, originalTopic, 
                "Max retries exceeded: " + errorMessage, null);
        } else {
            log.info("Retrying message from topic: {} (attempt {} of {})", 
                originalTopic, retryCount + 1, maxRetries);
            // This would typically trigger a retry mechanism
            scheduleRetry(message, originalTopic, retryCount + 1);
        }
    }
    
    private void scheduleRetry(Object message, String originalTopic, int retryCount) {
        // In a production system, this might use a delay queue or scheduler
        log.info("Retry scheduled for message from topic: {} (retry {})", originalTopic, retryCount);
        
        // For now, we'll just log. In production, implement with:
        // - Kafka delay topics
        // - Spring @Retryable
        // - External scheduler like Quartz
    }
    
    private String getStackTrace(Exception exception) {
        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            exception.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return "Error getting stack trace: " + e.getMessage();
        }
    }
    
    private String getServiceName() {
        // Try to determine service name from system properties or environment
        String serviceName = System.getProperty("spring.application.name");
        if (serviceName == null) {
            serviceName = System.getenv("SERVICE_NAME");
        }
        return serviceName != null ? serviceName : "unknown-service";
    }
    
    private void storeCriticalFailure(Object message, String originalTopic, String errorMessage, Exception dlqException) {
        // Critical failure - DLQ is down. Store locally or in emergency storage
        log.error("CRITICAL: Cannot store in DLQ! Topic: {}, Error: {}, DLQ Error: {}", 
            originalTopic, errorMessage, dlqException.getMessage());
        
        // In production, write to:
        // - Local file system
        // - Database
        // - Alternative message queue
        // - Monitoring system alert
        
        try {
            String criticalLog = String.format(
                "CRITICAL_FAILURE: timestamp=%s, topic=%s, error=%s, message=%s",
                LocalDateTime.now(),
                originalTopic,
                errorMessage,
                objectMapper.writeValueAsString(message)
            );
            
            // For now, just log to file (would be configurable in production)
            log.error("CRITICAL_FAILURE_LOG: {}", criticalLog);
            
        } catch (Exception logException) {
            log.error("Failed to log critical failure - system in degraded state!", logException);
        }
    }
}
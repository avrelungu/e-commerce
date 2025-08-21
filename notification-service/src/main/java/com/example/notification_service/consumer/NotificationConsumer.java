package com.example.notification_service.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final ObjectMapper objectMapper;

    public NotificationConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topicPattern = "^(order|payment|shipping)-.*$")
    public void notificationConsumer(String notificationEvent) {
        try {
            JsonNode notificationEventPayload = objectMapper.readTree(notificationEvent).get("payload");

            
        } catch (Exception e) {

        }
    }
}

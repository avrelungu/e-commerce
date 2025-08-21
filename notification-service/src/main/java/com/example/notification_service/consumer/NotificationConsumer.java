package com.example.notification_service.consumer;

import com.example.notification_service.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class NotificationConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public NotificationConsumer(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "#{kafkaTopics.orderCreated}")
    public void handleOrderCreated(String event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        processNotificationEvent(event, topic, "order-created");
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentProcessed}")
    public void handlePaymentProcessed(String event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        processNotificationEvent(event, topic, "payment-processed");
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentFailed}")
    public void handlePaymentFailed(String event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        processNotificationEvent(event, topic, "payment-failed");
    }

    @KafkaListener(topics = "shipping-order-shipped")
    public void handleOrderShipped(String event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        processNotificationEvent(event, topic, "shipping-order-shipped");
    }

    @KafkaListener(topics = "shipping-order-delivered")
    public void handleOrderDelivered(String event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        processNotificationEvent(event, topic, "shipping-order-delivered");
    }

    @KafkaListener(topics = "shipping-tracking-update")
    public void handleTrackingUpdate(String event, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        processNotificationEvent(event, topic, "shipping-tracking-update");
    }

    private void processNotificationEvent(String eventMessage, String topic, String eventType) {
        try {
            log.info("Received {} event from topic: {}", eventType, topic);

            JsonNode domainEvent = objectMapper.readTree(eventMessage);
            JsonNode payload = domainEvent.get("payload");

            if (payload == null) {
                log.warn("No payload found in event: {}", eventMessage);
                return;
            }

            // Extract common fields
            String eventId = domainEvent.has("eventId") ? domainEvent.get("eventId").asText() : UUID.randomUUID().toString();
            String orderId = extractOrderId(payload);

            if (orderId == null) {
                log.warn("No orderId found in event payload: {}", payload);
                return;
            }

            // Build event data map for template variables
            Map<String, String> eventData = buildEventDataMap(payload, eventType);

            // Process the notification
            notificationService.processOrderEvent(eventType, UUID.fromString(orderId), eventData);

            log.info("Successfully processed {} event for order: {}", eventType, orderId);

        } catch (Exception e) {
            log.error("Error processing notification event from topic {}: {}", topic, eventMessage, e);
        }
    }

    private String extractOrderId(JsonNode payload) {
        // Try different field names based on event type
        if (payload.has("orderId")) {
            return payload.get("orderId").asText();
        }
        if (payload.has("order_id")) {
            return payload.get("order_id").asText();
        }
        if (payload.has("id")) {
            return payload.get("id").asText();
        }
        return null;
    }

    private Map<String, String> buildEventDataMap(JsonNode payload, String eventType) {
        Map<String, String> eventData = new HashMap<>();

        // Add common fields
        addFieldIfExists(eventData, payload, "orderId", "orderNumber");
        addFieldIfExists(eventData, payload, "id", "orderNumber");

        // Event-specific data extraction
        switch (eventType) {
            case "order-created":
                addFieldIfExists(eventData, payload, "totalAmount", "totalAmount");
                addFieldIfExists(eventData, payload, "itemCount", "itemCount");
                addFieldIfExists(eventData, payload, "status", "status");
                break;

            case "payment-processed":
                addFieldIfExists(eventData, payload, "amount", "amount");
                addFieldIfExists(eventData, payload, "paymentMethod", "paymentMethod");
                addFieldIfExists(eventData, payload, "transactionId", "transactionId");
                break;

            case "shipping-order-shipped":
                addFieldIfExists(eventData, payload, "trackingNumber", "trackingNumber");
                addFieldIfExists(eventData, payload, "carrier", "carrier");
                addFieldIfExists(eventData, payload, "estimatedDelivery", "estimatedDelivery");
                eventData.put("trackingUrl", "https://example.com/track/" + eventData.get("trackingNumber"));
                break;

            case "shipping-order-delivered":
                addFieldIfExists(eventData, payload, "trackingNumber", "trackingNumber");
                addFieldIfExists(eventData, payload, "deliveredAt", "deliveredAt");
                addFieldIfExists(eventData, payload, "deliveryLocation", "deliveryLocation");
                break;
        }

        // Add mock customer data (in production, this would be fetched from order service)
        eventData.putIfAbsent("customerEmail", "customer@example.com");
        eventData.putIfAbsent("customerPhone", "+1-555-0123");

        return eventData;
    }

    private void addFieldIfExists(Map<String, String> eventData, JsonNode payload, String jsonField, String mapKey) {
        if (payload.has(jsonField) && !payload.get(jsonField).isNull()) {
            eventData.put(mapKey, payload.get(jsonField).asText());
        }
    }
}

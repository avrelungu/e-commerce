package com.example.shipping_service.consumer;

import com.example.shipping_service.model.Shipment;
import com.example.shipping_service.service.ShippingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class PaymentProcessedConsumer {

    private final ShippingService shippingService;
    private final ObjectMapper objectMapper;

    public PaymentProcessedConsumer(ShippingService shippingService, ObjectMapper objectMapper) {
        this.shippingService = shippingService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentProcessed}")
    public void handlePaymentProcessed(String eventMessage) {
        try {
            log.info("Received payment processed event: {}", eventMessage);
            
            JsonNode domainEvent = objectMapper.readTree(eventMessage);
            JsonNode payload = domainEvent.get("payload");
            
            String eventId = domainEvent.get("eventId").asText();
            String orderId = payload.get("orderId").asText();
            
            log.info("Processing payment processed event {} for order: {}", eventId, orderId);

            if (shippingService.findByOrderId(UUID.fromString(orderId)).isPresent()) {
                log.warn("Shipment already exists for order: {}, skipping", orderId);
                return;
            }

            JsonNode shippingAddress = createMockShippingAddress();

            Shipment shipment = shippingService.createShipment(UUID.fromString(orderId), shippingAddress);
            log.info("Created shipment {} for order: {}", shipment.getId(), orderId);
        } catch (Exception e) {
            log.error("Error processing payment processed event: {}", eventMessage, e);
        }
    }

    private JsonNode createMockShippingAddress() {
        try {
            String mockAddress = """
                {
                    "street": "123 Main Street",
                    "city": "Sample City",
                    "state": "CA",
                    "zipCode": "12345",
                    "country": "USA"
                }
                """;
            return objectMapper.readTree(mockAddress);
        } catch (Exception e) {
            throw new RuntimeException("Error creating mock shipping address", e);
        }
    }
}
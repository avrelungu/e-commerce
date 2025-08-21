package com.example.inventory_service.consumer;

import com.example.inventory_service.service.StockReservationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class PaymentFailedEventConsumer {

    private final StockReservationService stockReservationService;
    private final ObjectMapper objectMapper;

    public PaymentFailedEventConsumer(StockReservationService stockReservationService, ObjectMapper objectMapper) {
        this.stockReservationService = stockReservationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentFailed}")
    public void paymentFailedEventConsumer(String paymentFailedEvent) {
        try {
            JsonNode domainEventPayload = objectMapper.readTree(paymentFailedEvent).get("payload");

            String orderId = domainEventPayload.get("orderId").asText();

            stockReservationService.releaseReservation(UUID.fromString(orderId));
        } catch (Exception e) {
            log.info("Stock Release failed: {}", e.getMessage());
        }
    }
}

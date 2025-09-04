package com.example.inventory_service.consumer;

import com.example.inventory_service.service.StockReservationService;
import com.example.shared_common.idempotency.EventIdempotencyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class PaymentProcessedEventConsumer {

    private final ObjectMapper objectMapper;
    private final StockReservationService stockReservationService;
    private final EventIdempotencyService eventIdempotencyService;

    public PaymentProcessedEventConsumer(ObjectMapper objectMapper, StockReservationService stockReservationService, EventIdempotencyService eventIdempotencyService) {
        this.objectMapper = objectMapper;
        this.stockReservationService = stockReservationService;
        this.eventIdempotencyService = eventIdempotencyService;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentProcessed}")
    public void paymentProcessedEventConsumer(String paymentProcessedEvent) {
        try {
            JsonNode domainEvent = objectMapper.readTree(paymentProcessedEvent);
            String eventId = domainEvent.get("eventId").asText();
            JsonNode domaineEventPayload = objectMapper.readTree(paymentProcessedEvent).get("payload");

            String orderId = domaineEventPayload.get("orderId").asText();

            boolean processed = eventIdempotencyService.processOnce(eventId, () -> {
                stockReservationService.confirmReservation(UUID.fromString(orderId));
            });

            if (!processed) {
                log.info("Payment processed event already handled {} skipping confirmation for order {}", paymentProcessedEvent, orderId);
            }
        } catch (Exception e) {
            log.error("Stock confirmation failed: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

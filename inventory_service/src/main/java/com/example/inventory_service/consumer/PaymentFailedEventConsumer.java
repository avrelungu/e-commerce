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
public class PaymentFailedEventConsumer {

    private final StockReservationService stockReservationService;
    private final ObjectMapper objectMapper;
    private final EventIdempotencyService eventIdempotencyService;

    public PaymentFailedEventConsumer(StockReservationService stockReservationService, ObjectMapper objectMapper, EventIdempotencyService eventIdempotencyService) {
        this.stockReservationService = stockReservationService;
        this.objectMapper = objectMapper;
        this.eventIdempotencyService = eventIdempotencyService;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentFailed}")
    public void paymentFailedEventConsumer(String paymentFailedEvent) {
        try {
            JsonNode domainEvent = objectMapper.readTree(paymentFailedEvent);
            String eventId = domainEvent.get("eventId").asText();
            JsonNode domainEventPayload = objectMapper.readTree(paymentFailedEvent).get("payload");
            String orderId = domainEventPayload.get("orderId").asText();

            boolean processed = eventIdempotencyService.processOnce(eventId, () -> {
                stockReservationService.releaseReservation(UUID.fromString(orderId));
                log.info("Reservation has been released for orderId: {}", orderId);
            });

            if (!processed) {
                log.info("Payment failed event already processed, skipping stock release for event {}", eventId);
            }

        } catch (Exception e) {
            log.info("Stock Release failed: {}", e.getMessage());
        }
    }
}

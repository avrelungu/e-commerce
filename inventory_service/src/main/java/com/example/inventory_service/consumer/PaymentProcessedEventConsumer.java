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
public class PaymentProcessedEventConsumer {

    private final ObjectMapper objectMapper;
    private final StockReservationService stockReservationService;

    public PaymentProcessedEventConsumer(ObjectMapper objectMapper, StockReservationService stockReservationService) {
        this.objectMapper = objectMapper;
        this.stockReservationService = stockReservationService;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentProcessed}")
    public void paymentProcessedEventConsumer(String paymentProcessedEvent) {
        try {
            JsonNode domaineEventPayload = objectMapper.readTree(paymentProcessedEvent).get("payload");

            String orderId = domaineEventPayload.get("orderId").asText();

            stockReservationService.confirmReservation(UUID.fromString(orderId));
        } catch (Exception e) {
            log.error("Stock confirmation failed: {}", e.getMessage());
        }
    }
}

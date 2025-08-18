package com.example.payment_service.consumer;

import com.example.payment_service.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderPaymentEventConsumer {
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public OrderPaymentEventConsumer(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentRequest}")
    public void paymentRequestEventProcess(String event) {
        try {
            JsonNode domainEvent = objectMapper.readTree(event).get("payload");

            String orderId = domainEvent.get("orderId").asText();

            log.info("Received payment request event for order: {}", orderId);
        } catch (Exception exception) {
            log.error("Stock Reservation consumer failed: {}", exception.getMessage());
        }
    }
}

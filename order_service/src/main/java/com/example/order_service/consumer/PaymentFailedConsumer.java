package com.example.order_service.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentFailedConsumer {
    private final ObjectMapper objectMapper;

    public PaymentFailedConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentFailed}")
    public void paymentFailedConsumer(String paymentFailedEvent) {
        try {
            JsonNode domainEventPayload = objectMapper.readTree(paymentFailedEvent).get("payload");

            String orderId = domainEventPayload.get("orderId").asText();

            log.info("payment failed for order {} domainEventPayload: {}", orderId, domainEventPayload);

        } catch (Exception e) {

        }
    }
}

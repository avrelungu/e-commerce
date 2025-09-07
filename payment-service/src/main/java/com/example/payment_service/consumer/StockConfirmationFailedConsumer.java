package com.example.payment_service.consumer;

import com.example.payment_service.service.RefundService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StockConfirmationFailedConsumer {
    private final RefundService refundService;

    public StockConfirmationFailedConsumer(RefundService refundService) {
        this.refundService = refundService;
    }

    @KafkaListener(topics = "#{kafkaTopics.stockConfirmationFailed}")
    public void stockConfirmationFailed(String event) {
        try {
            log.info("need refund for: " + event);
            // refund here
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}

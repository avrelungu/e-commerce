package com.example.payment_service.consumer;

import com.example.payment_service.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StockReservedEventConsumer {
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public StockReservedEventConsumer(PaymentService paymentService, ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    public void stockReservedEventProcess(String event) {
        try {
            JsonNode domainEvent = objectMapper.readTree(event);

            log.info("Received stock reservation event: {}", domainEvent);
        } catch (Exception exception) {
            log.error("Stock Reservation consumer failed: {}", exception.getMessage());
        }
    }
}

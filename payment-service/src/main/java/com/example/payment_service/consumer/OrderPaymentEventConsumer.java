package com.example.payment_service.consumer;

import com.example.events.payment.PaymentFailedEvent;
import com.example.events.payment.PaymentProcessedEvent;
import com.example.events.payment.PaymentRequestEvent;
import com.example.events.common.Money;
import com.example.payment_service.mapper.PaymentRequestMapper;
import com.example.payment_service.publisher.EventPublisher;
import com.example.payment_service.service.PaymentService;
import com.example.shared_common.idempotency.EventIdempotencyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.data.Json;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class OrderPaymentEventConsumer {
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;
    private final PaymentRequestMapper paymentRequestMapper;
    private final EventIdempotencyService eventIdempotencyService;

    @Value("#{kafkaTopics.paymentProcessed}")
    private String paymentProcessedTopic;

    @Value("#{kafkaTopics.paymentFailed}")
    private String paymentFailedTopic;

    public OrderPaymentEventConsumer(PaymentService paymentService, ObjectMapper objectMapper, EventPublisher eventPublisher, PaymentRequestMapper paymentRequestMapper, EventIdempotencyService eventIdempotencyService) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.paymentRequestMapper = paymentRequestMapper;
        this.eventIdempotencyService = eventIdempotencyService;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentRequest}")
    public void paymentRequestEventProcess(String event) {
        try {
            JsonNode domainEvent = objectMapper.readTree(event);
            String eventId = domainEvent.get("eventId").asText();
            JsonNode domainEventPayload = objectMapper.readTree(event).get("payload");

            boolean processed = eventIdempotencyService.processOnce(eventId, () -> {
                PaymentRequestEvent paymentRequest = parsePaymentRequest(domainEventPayload);

                paymentService.processPayment(paymentRequest);
            });

            if (!processed) {
                log.error("Payment processing already handled for order {}", domainEventPayload.get("orderId").asText());
            }

        } catch (Exception exception) {
            log.error("Payment request processing failed: {}", exception.getMessage(), exception);
            throw new RuntimeException(exception);
        }
    }

    private PaymentRequestEvent parsePaymentRequest(JsonNode payload) {
        return PaymentRequestEvent.newBuilder()
                .setOrderId(payload.get("orderId").asText())
                .setAmount(Money.newBuilder()
                        .setAmount(new BigDecimal(payload.get("amount").get("amount").asText()))
                        .setCurrency(payload.get("amount").get("currency").asText())
                        .build())
                .setPaymentMethodToken(payload.get("paymentMethodToken").asText())
                .setPaymentMethodType(payload.get("paymentMethodType").asText())
                .build();
    }
}

package com.example.payment_service.consumer;

import com.example.events.payment.PaymentFailedEvent;
import com.example.events.payment.PaymentProcessedEvent;
import com.example.events.payment.PaymentRequestEvent;
import com.example.events.common.Money;
import com.example.payment_service.mapper.PaymentRequestMapper;
import com.example.payment_service.publisher.EventPublisher;
import com.example.payment_service.service.PaymentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

    @Value("#{kafkaTopics.paymentProcessed}")
    private String paymentProcessedTopic;
    
    @Value("#{kafkaTopics.paymentFailed}")
    private String paymentFailedTopic;

    public OrderPaymentEventConsumer(PaymentService paymentService, ObjectMapper objectMapper, EventPublisher eventPublisher, PaymentRequestMapper paymentRequestMapper) {
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.paymentRequestMapper = paymentRequestMapper;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentRequest}")
    public void paymentRequestEventProcess(String event) {
        try {
            JsonNode domainEventPayload = objectMapper.readTree(event).get("payload");

            PaymentRequestEvent paymentRequest = parsePaymentRequest(domainEventPayload);

            paymentService.processPayment(paymentRequest);
//
//            if (result instanceof PaymentProcessedEvent successEvent) {
//                eventPublisher.publishPaymentProcessed(paymentProcessedTopic, successEvent.getOrderId(), successEvent);
//                log.info("Published PaymentProcessed event for order: {}", successEvent.getOrderId());
//            } else if (result instanceof PaymentFailedEvent failureEvent) {
//                eventPublisher.publishPaymentFailed(paymentFailedTopic, failureEvent.getOrderId(), failureEvent);
//                log.warn("Published PaymentFailed event for order: {} with reason: {}",
//                    failureEvent.getOrderId(), failureEvent.getFailureReason());
//            } else {
//                log.error("Unexpected payment result type: {}", result.getClass().getSimpleName());
//            }
            
        } catch (Exception exception) {
            log.error("Payment request processing failed: {}", exception.getMessage(), exception);
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

package com.example.inventory_service.consumer;

import com.example.events.payment.PaymentProcessedEvent;
import com.example.inventory_service.event.StockConfirmationFailed;
import com.example.inventory_service.exception.InsufficientStockException;
import com.example.inventory_service.publisher.EventPublisher;
import com.example.inventory_service.service.StockReservationService;
import com.example.shared_common.idempotency.EventIdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class PaymentProcessedEventConsumer {

    @Value("${kafka.topics.stock-confirmation-failed}")
    private String stockConfirmationFailedTopic;

    private final StockReservationService stockReservationService;
    private final EventIdempotencyService eventIdempotencyService;
    private final EventPublisher eventPublisher;

    public PaymentProcessedEventConsumer(
            StockReservationService stockReservationService,
            EventIdempotencyService eventIdempotencyService,
            EventPublisher eventPublisher
    ) {
        this.stockReservationService = stockReservationService;
        this.eventIdempotencyService = eventIdempotencyService;
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentProcessed}")
    public void paymentProcessedEventConsumer(PaymentProcessedEvent paymentProcessedEvent) {
        try {
            String orderId = paymentProcessedEvent.getOrderId();

            boolean processed = eventIdempotencyService.processOnce("reservation-confirmation-order-" + orderId, () -> {
                try {
                    stockReservationService.confirmReservation(UUID.fromString(orderId));
                } catch (InsufficientStockException e) {
                    log.error("Reservation confirmations failed: {}", e.getMessage());

                    eventPublisher.publish(new StockConfirmationFailed(stockConfirmationFailedTopic, paymentProcessedEvent, orderId));
                }
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

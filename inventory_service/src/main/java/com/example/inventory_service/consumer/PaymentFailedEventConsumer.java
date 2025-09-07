package com.example.inventory_service.consumer;

import com.example.events.payment.PaymentFailedEvent;
import com.example.inventory_service.service.StockReservationService;
import com.example.shared_common.idempotency.EventIdempotencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class PaymentFailedEventConsumer {

    private final StockReservationService stockReservationService;
    private final EventIdempotencyService eventIdempotencyService;

    public PaymentFailedEventConsumer(
            StockReservationService stockReservationService,
            EventIdempotencyService eventIdempotencyService
    ) {
        this.stockReservationService = stockReservationService;
        this.eventIdempotencyService = eventIdempotencyService;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentFailed}")
    public void paymentFailedEventConsumer(PaymentFailedEvent paymentFailedEvent) {
        try {
            String orderId = paymentFailedEvent.getOrderId();

            boolean processed = eventIdempotencyService.processOnce(orderId, () -> {
                stockReservationService.releaseReservation(UUID.fromString(orderId));
                log.info("Reservation has been released for orderId: {}", orderId);
            });

            if (!processed) {
                log.info("Payment failed event already processed, skipping stock release for order {}", orderId);
            }

        } catch (Exception e) {
            log.info("Stock Release failed: {}", e.getMessage());
        }
    }
}

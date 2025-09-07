package com.example.payment_service.consumer;

import com.example.events.payment.PaymentRequestEvent;
import com.example.payment_service.service.PaymentService;
import com.example.shared_common.idempotency.EventIdempotencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderPaymentEventConsumer {
    private final PaymentService paymentService;
    private final EventIdempotencyService eventIdempotencyService;

    public OrderPaymentEventConsumer(
            PaymentService paymentService,
            EventIdempotencyService eventIdempotencyService
    ) {
        this.paymentService = paymentService;
        this.eventIdempotencyService = eventIdempotencyService;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentRequest}")
    public void paymentRequestEventProcess(PaymentRequestEvent paymentRequest) {
        try {
            String eventId = paymentRequest.getOrderId();

            boolean processed = eventIdempotencyService.processOnce(
                    eventId,
                    () -> paymentService.processPayment(paymentRequest)
            );

            if (!processed) {
                log.error("Payment processing already handled for order {}", eventId);
            }

        } catch (Exception exception) {
            log.error("Payment request processing failed: {}", exception.getMessage(), exception);
            throw new RuntimeException(exception);
        }
    }
}

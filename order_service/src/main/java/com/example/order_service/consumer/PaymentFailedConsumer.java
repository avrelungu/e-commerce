package com.example.order_service.consumer;

import com.example.events.payment.PaymentFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentFailedConsumer {

    @KafkaListener(topics = "#{kafkaTopics.paymentFailed}")
    public void paymentFailedConsumer(PaymentFailedEvent paymentFailedEvent) {
        try {
            String orderId = paymentFailedEvent.getOrderId();

            log.info("payment failed for order {} domainEventPayload: {}", orderId, paymentFailedEvent);
        } catch (Exception e) {
            log.error("payment failed", e);
        }
    }
}

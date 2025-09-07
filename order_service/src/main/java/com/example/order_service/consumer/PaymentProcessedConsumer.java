package com.example.order_service.consumer;

import com.example.events.payment.PaymentProcessedEvent;
import com.example.order_service.enums.OrderStatus;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class PaymentProcessedConsumer {
    private final OrderRepository orderRepository;

    public PaymentProcessedConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentProcessed}")
    public void paymentProcessedConsumer(PaymentProcessedEvent paymentProcessedEvent) {
        try {
            String orderId = paymentProcessedEvent.getOrderId();

            Optional<Order> order = orderRepository.findById(UUID.fromString(orderId));

            if (order.isPresent()) {
                order.get().setStatus(String.valueOf(OrderStatus.PAID));
                orderRepository.save(order.get());
            }

            log.info("Domain payment paymentProcessedEvent received: {}", paymentProcessedEvent);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

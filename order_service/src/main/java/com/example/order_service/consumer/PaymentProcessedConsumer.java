package com.example.order_service.consumer;

import com.example.events.payment.PaymentProcessedEvent;
import com.example.order_service.enums.OrderStatus;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.OrderStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class PaymentProcessedConsumer {
    private final OrderRepository orderRepository;
    private final OrderStateService orderStateService;

    public PaymentProcessedConsumer(OrderRepository orderRepository, OrderStateService orderStateService) {
        this.orderRepository = orderRepository;
        this.orderStateService = orderStateService;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentProcessed}")
    public void paymentProcessedConsumer(PaymentProcessedEvent paymentProcessedEvent) {
        try {
            String orderId = paymentProcessedEvent.getOrderId();

            Optional<Order> order = orderRepository.findById(UUID.fromString(orderId));

            order.ifPresent(value -> orderStateService.updateOrderStatus(value, OrderStatus.PAID));

            log.info("Domain payment paymentProcessedEvent received: {}", paymentProcessedEvent);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

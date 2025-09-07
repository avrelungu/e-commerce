package com.example.order_service.consumer;

import com.example.events.inventory.OutOfStockEvent;
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
public class OutOfStockConsumer {

    private final OrderRepository orderRepository;

    public OutOfStockConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "#{kafkaTopics.outOfStock}")
    private void outOfStockConsumer(OutOfStockEvent outOfStockEvent) {
        try {
            String orderId = outOfStockEvent.getOrderId();

            Optional<Order> order = orderRepository.findById(UUID.fromString(orderId));

            if (order.isEmpty()) {
                log.error("Order {} not found for outOfStockEvent: {}", orderId, outOfStockEvent);

                return;
            }

            order.get().setStatus(OrderStatus.OUT_OF_STOCK.name());
            orderRepository.save(order.get());
        } catch (Exception e) {
            log.error("Error while processing outOfStockEvent: {}", outOfStockEvent, e);
        }
    }
}

package com.example.order_service.consumer;

import com.example.order_service.enums.OrderStatus;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.OrderStateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class OrderShippedConsumer {
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final OrderStateService orderStateService;

    public OrderShippedConsumer(ObjectMapper objectMapper, OrderRepository orderRepository, OrderStateService orderStateService) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
        this.orderStateService = orderStateService;
    }

    @KafkaListener(topics = "#{kafkaTopics.orderShipped}")
    public void orderShippedConsumer(String orderShippedEvent) {
        try {
            JsonNode domainEventPayload = objectMapper.readTree(orderShippedEvent).get("payload");

            String orderId = domainEventPayload.get("orderId").asText();

            Optional<Order> order = orderRepository.findById(UUID.fromString(orderId));

            if (order.isEmpty()) {
                log.info("Order shipped event: order not found: {}", orderId);

                return;
            }

            orderStateService.updateOrderStatus(order.get(), OrderStatus.SHIPPED);
        } catch (Exception e) {
            log.error("Order shipped consumer failed: {}", e.getMessage());
        }
    }
}

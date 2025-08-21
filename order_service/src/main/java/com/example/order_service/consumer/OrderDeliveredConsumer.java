package com.example.order_service.consumer;

import com.example.order_service.enums.OrderStatus;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class OrderDeliveredConsumer {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    public OrderDeliveredConsumer(ObjectMapper objectMapper, OrderRepository orderRepository) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "#{kafkaTopics.orderDelivered}")
    public void orderDeliveredConsumer(String orderDeliveredEvent) {
        try {
            JsonNode domainEventPayload = objectMapper.readTree(orderDeliveredEvent).get("payload");

            String orderId = domainEventPayload.get("orderId").asText();

            Optional<Order> order = orderRepository.findById(UUID.fromString(orderId));

            if (order.isEmpty()) {
                log.error("Order id {} not found", orderId);

                return;
            }

            order.get().setStatus(OrderStatus.DELIVERED.name());
            
            orderRepository.save(order.get());
        } catch (Exception e) {
            log.error("Order delivered consumer failed: {}", e.getMessage());
        }
    }
}

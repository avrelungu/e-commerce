package com.example.order_service.consumer;

import com.example.order_service.enums.OrderStatus;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class PaymentProcessedConsumer {
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    public PaymentProcessedConsumer(ObjectMapper objectMapper, OrderRepository orderRepository) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "#{kafkaTopics.paymentProcessed}")
    public void paymentProcessedConsumer(String event) {
        try {
            JsonNode domainEventPayload = objectMapper.readTree(event).get("payload");

            String orderId = domainEventPayload.get("orderId").asText();
            
            Optional<Order> order = orderRepository.findById(UUID.fromString(orderId));

            if (order.isPresent()) {
                order.get().setStatus(String.valueOf(OrderStatus.PAID));
                orderRepository.save(order.get());
            }

            log.info("Domain payment event received: {}", domainEventPayload);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

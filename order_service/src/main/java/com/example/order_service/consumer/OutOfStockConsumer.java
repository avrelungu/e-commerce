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
public class OutOfStockConsumer {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    public OutOfStockConsumer(ObjectMapper objectMapper, OrderRepository orderRepository) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "#{kafkaTopics.outOfStock}")
    private void outOfStockConsumer(String outOfStockEvent) {
        try {
            JsonNode domainEventPayload = objectMapper.readTree(outOfStockEvent).get("payload");

            String orderId = domainEventPayload.get("orderId").asText();

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

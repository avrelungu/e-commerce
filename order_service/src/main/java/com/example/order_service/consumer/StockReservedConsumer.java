package com.example.order_service.consumer;

import com.example.order_service.dto.event.PaymentRequestEventDto;
import com.example.order_service.enums.OrderStatus;
import com.example.order_service.event.PaymentRequest;
import com.example.order_service.exceptions.AppException;
import com.example.order_service.mapper.PaymentRequestMapper;
import com.example.order_service.model.Order;
import com.example.order_service.publisher.EventPublisher;
import com.example.order_service.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class StockReservedConsumer {
    private final EventPublisher eventPublisher;
    private final PaymentRequestMapper paymentRequestMapper;
    @Value("#{kafkaTopics.paymentRequest}")
    private String paymentRequestTopic;

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    public StockReservedConsumer(ObjectMapper objectMapper, OrderRepository orderRepository, EventPublisher eventPublisher, PaymentRequestMapper paymentRequestMapper) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.paymentRequestMapper = paymentRequestMapper;
    }

    @KafkaListener(topics = "#{kafkaTopics.stockReserved}", groupId = "order-service-inventory-processor")
    public void stockReservedConsume(String event) throws JsonProcessingException {
        try {
            log.info("Stock reserved event: {}", event);

            JsonNode domainEvent = objectMapper.readTree(event);

            String orderId = domainEvent.get("payload").get("orderId").asText();

            Optional<Order> order = orderRepository.findById(UUID.fromString(orderId));

            if (order.isEmpty()) {
                throw new AppException("Order not found", HttpStatus.NOT_FOUND);
            }

            order.get().setStatus(String.valueOf(OrderStatus.CONFIRMED));

            orderRepository.save(order.get());

            PaymentRequestEventDto paymentRequestEventDto = paymentRequestMapper.toPaymentRequestEvent(order.get());

            eventPublisher.publish(new PaymentRequest(paymentRequestTopic, orderId, paymentRequestEventDto));
        } catch (AppException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Error processing order created event for order:");
            // TODO: Publish error event or handle retry logic
        }
    }
}

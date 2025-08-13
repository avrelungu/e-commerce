package com.example.inventory_service.consumer;

import com.example.events.order.OrderCreatedEvent;
import com.example.inventory_service.dto.ReservationRequestDto;
import com.example.inventory_service.service.StockReservationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class OrderEventConsumer {

    private final StockReservationService stockReservationService;

    public OrderEventConsumer(StockReservationService stockReservationService) {
        this.stockReservationService = stockReservationService;
    }

    @KafkaListener(topics = "${order.events.order-created}")
    public void orderCreateListener(String event) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode domainEvent = objectMapper.readTree(event);

        JsonNode payload = domainEvent.get("payload");

        OrderCreatedEvent orderCreatedEvent = objectMapper.treeToValue(payload, OrderCreatedEvent.class);

        log.info("Order created event: {}: ", orderCreatedEvent);

        try {
            List<ReservationRequestDto> reservationRequests = orderCreatedEvent.getItems()
                    .stream()
                    .map(item -> new ReservationRequestDto(
                            UUID.fromString(item.getProductId()), 
                            item.getQuantity()))
                    .toList();

            boolean reservationSuccessful = stockReservationService.reserveStock(
                    UUID.fromString(orderCreatedEvent.getOrderId()), 
                    reservationRequests);

            if (reservationSuccessful) {
                log.info("Stock reservation successful for order: {}", orderCreatedEvent.getOrderId());
                // TODO: Publish StockReservedEvent
            } else {
                log.warn("Stock reservation failed for order: {}", orderCreatedEvent.getOrderId());
                // TODO: Publish OutOfStockEvent
            }
            
        } catch (Exception e) {
            log.error("Error processing order created event for order: {}", orderCreatedEvent.getOrderId(), e);
            // TODO: Publish error event or handle retry logic
        }
    }
}

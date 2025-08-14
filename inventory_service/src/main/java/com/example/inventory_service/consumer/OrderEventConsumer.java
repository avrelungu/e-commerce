package com.example.inventory_service.consumer;

import com.example.events.inventory.StockReservedEvent;
import com.example.events.order.OrderCreatedEvent;
import com.example.inventory_service.dto.ReservationRequestDto;
import com.example.inventory_service.dto.event.StockReservedEventDto;
import com.example.inventory_service.event.StockReserved;
import com.example.inventory_service.mapper.StockReservationMapper;
import com.example.inventory_service.model.StockReservation;
import com.example.inventory_service.publisher.EventPublisher;
import com.example.inventory_service.service.StockReservationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class OrderEventConsumer {
    @Value("#{kafkaTopics.stockReserved}")
    private String stockReservedTopic;

    private final StockReservationService stockReservationService;
    private final StockReservationMapper stockReservationMapper;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(StockReservationService stockReservationService, StockReservationMapper stockReservationMapper, EventPublisher eventPublisher, ObjectMapper objectMapper) {
        this.stockReservationService = stockReservationService;
        this.stockReservationMapper = stockReservationMapper;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "#{kafkaTopics.orderCreated}")
    public void orderCreateListener(String event) throws JsonProcessingException {
        JsonNode domainEvent = objectMapper.readTree(event);

        JsonNode payload = domainEvent.get("payload");

        String eventId = domainEvent.get("eventId").asText();

        log.info("Received orderCreated eventId {}", eventId);

        OrderCreatedEvent orderCreatedEvent = objectMapper.treeToValue(payload, OrderCreatedEvent.class);

        String orderId = orderCreatedEvent.getOrderId();

        log.info("Order created event: {}: ", orderCreatedEvent);

        try {
            List<ReservationRequestDto> reservationRequests = orderCreatedEvent.getItems()
                    .stream()
                    .map(item -> new ReservationRequestDto(
                            UUID.fromString(item.getProductId()), 
                            item.getQuantity()))
                    .toList();

            List<StockReservation> reservations = stockReservationService.reserveStock(
                    UUID.fromString(orderCreatedEvent.getOrderId()), 
                    reservationRequests
            );

            if (!reservations.isEmpty()) {
                log.info("Stock reservation successful for order: {}", orderCreatedEvent.getOrderId());
                StockReservedEvent stockReservedEvent = stockReservationMapper.toStockReservedEvent(reservations, eventId, orderId);
                log.info("Stock reserved event: {}: ", stockReservedEvent);

                StockReservedEventDto stockReservedEventDto = stockReservationMapper.toStockReservedEventDto(stockReservedEvent);

                eventPublisher.publish(new StockReserved(stockReservedTopic, stockReservedEventDto, orderId));
                // TODO: Populate stockReservedEvent with reservation data and publish
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

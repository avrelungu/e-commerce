package com.example.inventory_service.consumer;

import com.example.events.order.OrderCreatedEvent;
import com.example.inventory_service.dto.ReservationRequestDto;
import com.example.inventory_service.dto.event.StockReservedEventDto;
import com.example.inventory_service.event.StockReserved;
import com.example.inventory_service.mapper.StockReservationMapper;
import com.example.inventory_service.model.StockReservation;
import com.example.inventory_service.publisher.EventPublisher;
import com.example.inventory_service.service.StockReservationService;
import com.example.shared_common.idempotency.ApiIdempotencyService;
import com.example.shared_common.idempotency.EventIdempotencyService;
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
public class OrderCreatedEventConsumer {
    @Value("#{kafkaTopics.stockReserved}")
    private String stockReservedTopic;

    private final StockReservationService stockReservationService;
    private final StockReservationMapper stockReservationMapper;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final EventIdempotencyService eventIdempotencyService;

    public OrderCreatedEventConsumer(
            StockReservationService stockReservationService,
            StockReservationMapper stockReservationMapper,
            EventPublisher eventPublisher,
            ObjectMapper objectMapper,
            EventIdempotencyService eventIdempotencyService
    ) {
        this.stockReservationService = stockReservationService;
        this.stockReservationMapper = stockReservationMapper;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.eventIdempotencyService = eventIdempotencyService;
    }

    @KafkaListener(topics = "#{kafkaTopics.orderCreated}")
    public void orderCreateListener(String event) throws JsonProcessingException {
        JsonNode domainEvent = objectMapper.readTree(event);
        JsonNode payload = domainEvent.get("payload");
        String eventId = domainEvent.get("eventId").asText();

        log.info("Received orderCreated eventId {}", eventId);

        boolean processed = eventIdempotencyService.processOnce(eventId, () -> {
            try {
                OrderCreatedEvent orderCreatedEvent = objectMapper.treeToValue(payload, OrderCreatedEvent.class);

                String orderId = orderCreatedEvent.getOrderId();

                log.info("Order created event: {}: ", orderCreatedEvent);

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
                    com.example.events.inventory.StockReservedEvent stockReservedEvent = stockReservationMapper.toStockReservedEvent(reservations, orderId);

                    StockReservedEventDto stockReservedEventDto = stockReservationMapper.toStockReservedEventDto(stockReservedEvent);

                    eventPublisher.publish(new StockReserved(stockReservedTopic, stockReservedEventDto, orderId));
                }
            } catch (Exception e) {
                log.error("Error processing order created event", e);
                throw new RuntimeException(e);
            }
        });

        if (!processed) {
            log.info("Event {} already processed, skipping", eventId);
        }
    }
}

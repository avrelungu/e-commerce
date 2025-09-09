package com.example.inventory_service.consumer;

import com.example.events.inventory.StockReservedEvent;
import com.example.events.order.OrderCreatedEvent;
import com.example.inventory_service.dto.ReservationRequestDto;
import com.example.inventory_service.event.StockReserved;
import com.example.inventory_service.exception.InsufficientStockException;
import com.example.inventory_service.mapper.StockReservationMapper;
import com.example.inventory_service.model.StockReservation;
import com.example.inventory_service.publisher.EventPublisher;
import com.example.inventory_service.service.StockReservationService;
import com.example.shared_common.idempotency.EventIdempotencyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
    private final EventIdempotencyService eventIdempotencyService;

    public OrderCreatedEventConsumer(
            StockReservationService stockReservationService,
            StockReservationMapper stockReservationMapper,
            EventPublisher eventPublisher,
            EventIdempotencyService eventIdempotencyService
    ) {
        this.stockReservationService = stockReservationService;
        this.stockReservationMapper = stockReservationMapper;
        this.eventPublisher = eventPublisher;
        this.eventIdempotencyService = eventIdempotencyService;
    }

    @KafkaListener(topics = "#{kafkaTopics.orderCreated}")
    public void orderCreateListener(ConsumerRecord<String, OrderCreatedEvent> record) {
        OrderCreatedEvent orderCreatedEvent = record.value();
        String eventId = orderCreatedEvent.getOrderId();

        log.info("Received orderCreated eventId {}", eventId);

        boolean processed = eventIdempotencyService.processOnce("stock-reservation-order-" + eventId, () -> {
            try {

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
                    StockReservedEvent stockReservedEvent = stockReservationMapper.toStockReservedEvent(reservations, orderId);

                    eventPublisher.publish(new StockReserved(stockReservedTopic, stockReservedEvent, orderId));
                }
            } catch (Exception | InsufficientStockException e) {
                log.error("Error processing order created event", e);
            }
        });

        if (!processed) {
            log.info("Event {} already processed, skipping", eventId);
        }
    }
}

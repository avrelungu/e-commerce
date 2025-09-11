package com.example.order_service.consumer;

import com.example.events.inventory.StockReservedEvent;
import com.example.events.payment.PaymentRequestEvent;
import com.example.order_service.enums.OrderStatus;
import com.example.order_service.event.PaymentRequest;
import com.example.order_service.exceptions.AppException;
import com.example.order_service.mapper.PaymentRequestMapper;
import com.example.order_service.model.Order;
import com.example.order_service.publisher.EventPublisher;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.OrderStateService;
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
    private final OrderRepository orderRepository;
    private final OrderStateService orderStateService;
    
    @Value("#{kafkaTopics.paymentRequest}")
    private String paymentRequestTopic;

    public StockReservedConsumer(
            OrderRepository orderRepository,
            EventPublisher eventPublisher,
            PaymentRequestMapper paymentRequestMapper,
            OrderStateService orderStateService
    ) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.paymentRequestMapper = paymentRequestMapper;
        this.orderStateService = orderStateService;
    }

    @KafkaListener(topics = "#{kafkaTopics.stockReserved}", groupId = "order-service-inventory-processor")
    public void stockReservedConsumer(StockReservedEvent stockReservedEvent) {
        try {
            String orderId = stockReservedEvent.getOrderId();

            Optional<Order> order = orderRepository.findById(UUID.fromString(orderId));

            if (order.isEmpty()) {
                throw new AppException("Order not found", HttpStatus.NOT_FOUND);
            }

            // Use state machine to update order status
            orderStateService.updateOrderStatus(order.get(), OrderStatus.CONFIRMED);

            PaymentRequestEvent paymentRequestEvent = paymentRequestMapper.toPaymentRequestEvent(order.get());

            eventPublisher.publish(new PaymentRequest(paymentRequestTopic, paymentRequestEvent, orderId));
        } catch (AppException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Error processing order created stockReservedEvent for order:");
            throw e;
        }
    }
}

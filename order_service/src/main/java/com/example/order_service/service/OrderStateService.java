package com.example.order_service.service;

import com.example.order_service.enums.OrderStatus;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class OrderStateService {
    
    private final OrderRepository orderRepository;
    private final OrderStateMachine orderStateMachine;
    
    public OrderStateService(OrderRepository orderRepository, OrderStateMachine orderStateMachine) {
        this.orderRepository = orderRepository;
        this.orderStateMachine = orderStateMachine;
    }
    
    @Transactional
    public void updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        updateOrderStatus(order, newStatus);
    }
    
    @Transactional
    public void updateOrderStatus(Order order, OrderStatus newStatus) {
        OrderStatus currentStatus = OrderStatus.valueOf(order.getStatus());
        
        if (currentStatus == newStatus) {
            log.debug("Order {} already in status {}", order.getId(), newStatus);
            return;
        }

        orderStateMachine.validateTransition(order, newStatus);

        order.setStatus(String.valueOf(newStatus));
        order.setUpdatedAt(LocalDateTime.now());

        switch (newStatus) {
            case CONFIRMED:
                log.info("Order {} confirmed - stock reserved", order.getId());
                break;
            case PAID:
                log.info("Order {} paid - ready for shipment", order.getId());
                break;
            case SHIPPED:
                log.info("Order {} shipped - tracking available", order.getId());
                break;
            case DELIVERED:
                log.info("Order {} delivered successfully", order.getId());
                break;
            case CANCELLED:
                log.info("Order {} cancelled", order.getId());
                break;
            case REFUNDED:
                log.info("Order {} refunded", order.getId());
                break;
            case RETURNED:
                log.info("Order {} returned", order.getId());
                break;
            case OUT_OF_STOCK:
                log.warn("Order {} out of stock", order.getId());
                break;
            default:
                log.debug("Order {} status updated to {}", order.getId(), newStatus);
        }
        
        orderRepository.save(order);
        log.info("Order {} status updated from {} to {}", order.getId(), currentStatus, newStatus);
    }
}
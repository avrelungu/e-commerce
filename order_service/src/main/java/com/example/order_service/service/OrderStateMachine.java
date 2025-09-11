package com.example.order_service.service;

import com.example.order_service.enums.OrderStatus;
import com.example.order_service.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class OrderStateMachine {
    
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
        OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED, OrderStatus.OUT_OF_STOCK),
        OrderStatus.CONFIRMED, Set.of(OrderStatus.PAID, OrderStatus.CANCELLED),
        OrderStatus.PAID, Set.of(OrderStatus.SHIPPED, OrderStatus.REFUNDED),
        OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED, OrderStatus.RETURNED),
        OrderStatus.DELIVERED, Set.of(OrderStatus.RETURNED),
        OrderStatus.RETURNED, Set.of(OrderStatus.REFUNDED),
        OrderStatus.CANCELLED, Set.of(),
        OrderStatus.REFUNDED, Set.of(),
        OrderStatus.OUT_OF_STOCK, Set.of(OrderStatus.CANCELLED)
    );
    
    public boolean canTransition(OrderStatus from, OrderStatus to) {
        return VALID_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }
    
    public void validateTransition(Order order, OrderStatus newStatus) {
        if (!canTransition(order.getStatus(), newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid transition from %s to %s for order %s",
                    order.getStatus(), newStatus, order.getId())
            );
        }
        log.debug("Valid state transition from {} to {} for order {}", 
            order.getStatus(), newStatus, order.getId());
    }
    
    public Set<OrderStatus> getValidNextStates(OrderStatus currentStatus) {
        return VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
    }
}
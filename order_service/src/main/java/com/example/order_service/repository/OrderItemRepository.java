package com.example.order_service.repository;

import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> order(Order order);

    List<OrderItem> findAllByOrderId(UUID order_id);
}

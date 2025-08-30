package com.example.order_service.service;

import com.example.order_service.dto.OrderItemDto;
import com.example.order_service.mapper.OrderItemMapper;
import com.example.order_service.repository.OrderItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;

    public OrderItemService(OrderItemRepository orderItemRepository, OrderItemMapper orderItemMapper) {
        this.orderItemRepository = orderItemRepository;
        this.orderItemMapper = orderItemMapper;
    }

    public List<OrderItemDto> getOrderItems(String orderId) {

        return orderItemRepository.findAllByOrderId(UUID.fromString(orderId))
                .stream()
                .map(orderItemMapper::toOrderItemDto)
                .toList();
    }
}

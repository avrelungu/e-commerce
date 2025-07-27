package com.example.order_service.service;

import com.example.order_service.dto.CreateOrderDto;
import com.example.order_service.dto.OrderDto;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;

    public OrderService(OrderMapper orderMapper, OrderRepository orderRepository) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
    }

    public void createOrder(CreateOrderDto orderDto) {
        Order order = orderMapper.toOrderFromCreateOrderDto(orderDto);
        log.info("Order DTO: {}", orderDto.toString());
        log.info("Order: {}", order);
//        orderRepository.save(order);
    }
}

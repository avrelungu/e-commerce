package com.example.order_service.service;

import com.example.order_service.dto.CreateOrderDto;
import com.example.order_service.dto.OrderDto;
import com.example.order_service.dto.OrderItemDto;
import com.example.order_service.event.OrderCreated;
import com.example.order_service.exceptions.AppException;
import com.example.order_service.mapper.OrderEventMapper;
import com.example.order_service.mapper.OrderItemMapper;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;
import com.example.order_service.publisher.EventPublisher;
import com.example.order_service.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.events.order.OrderCreatedEvent;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class OrderService {
    private final OrderItemMapper orderItemMapper;

    @Value("#{kafkaTopics.orderCreated}")
    private String orderCreatedTopic;

    private final EventPublisher eventPublisher;

    private final OrderNumberGenerator orderNumberGenerator;

    private final OrderMapper orderMapper;

    private final OrderEventMapper orderEventMapper;

    private final OrderRepository orderRepository;

    public OrderService(
            OrderMapper orderMapper,
            OrderRepository orderRepository,
            OrderNumberGenerator orderNumberGenerator,
            EventPublisher eventPublisher,
            OrderItemMapper orderItemMapper,
            OrderEventMapper orderEventMapper
    ) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
        this.orderNumberGenerator = orderNumberGenerator;
        this.eventPublisher = eventPublisher;
        this.orderItemMapper = orderItemMapper;
        this.orderEventMapper = orderEventMapper;
    }

    public Order createOrder(CreateOrderDto orderDto) throws AppException {
        long startTime = System.currentTimeMillis();
        
        // Add context to MDC for structured logging
        MDC.put("customerId", String.valueOf(orderDto.getCustomerId()));
        MDC.put("itemCount", String.valueOf(orderDto.getItems().size()));
        
        try {
            log.info("🛒 Starting order creation for customer: {} with {} items", 
                orderDto.getCustomerId(), orderDto.getItems().size());
                
            Order order = buildOrder(orderDto);
            
            // Add orderId to MDC once available
            MDC.put("orderId", order.getId().toString());
            MDC.put("orderNumber", order.getOrderNumber());
            
            orderRepository.save(order);
            log.info("💾 Order persisted to database: {} (orderNumber: {})", 
                order.getId(), order.getOrderNumber());

            OrderCreatedEvent orderCreatedEvent = orderEventMapper.toOrderCreatedEvent(order);
            eventPublisher.publish(new OrderCreated(orderCreatedTopic, orderCreatedEvent, order.getId().toString()));
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ Order creation completed successfully: {} ({}ms) - Event published to topic: {}", 
                order.getId(), duration, orderCreatedTopic);
                
            // Log business metrics
            log.info("📊 BUSINESS_METRIC: order_created, orderId={}, customerId={}, totalAmount={}, itemCount={}, processingTime={}ms",
                order.getId(), orderDto.getCustomerId(), order.getTotalAmount(), 
                orderDto.getItems().size(), duration);

            return order;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ Order creation failed for customer: {} after {}ms", 
                orderDto.getCustomerId(), duration, e);
            throw e;
        } finally {
            // Clean up MDC
            MDC.remove("customerId");
            MDC.remove("itemCount");
            MDC.remove("orderId");
            MDC.remove("orderNumber");
        }
    }

    private Order buildOrder(CreateOrderDto orderDto) {
        Order order = orderMapper.createOrderDtoToOrder(orderDto);

        List<OrderItem> orderItemList = getOrderItems(orderDto.getItems(), order);

        order.setOrderItems(orderItemList);
        order.setOrderNumber(orderNumberGenerator.generateOrderNumber());
        order.setShippingAmount(orderDto.getPricing().getShippingAmount());
        order.setTaxAmount(orderDto.getPricing().getTaxAmount());
        order.setTotalAmount(orderDto.getPricing().getTotalAmount());

        return order;
    }

    private List<OrderItem> getOrderItems(List<OrderItemDto> orderItemDtoList, Order order) {
        return orderItemDtoList.stream()
                .map(dto -> orderItemMapper.toOrderItem(dto, order))
                .toList();
    }

    public Page<OrderDto> getOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return orderRepository.findAll(pageable)
                .map(orderMapper::toOrderDto);
    }
}

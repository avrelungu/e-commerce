package com.example.order_service.service;

import com.example.order_service.dto.CreateOrderDto;
import com.example.order_service.dto.OrderItemDto;
import com.example.order_service.dto.event.OrderCreatedEventDto;
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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${inventory_service.products.tax-rate}")
    private BigDecimal SALES_TAX_RATE;

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

    public void createOrder(CreateOrderDto orderDto) throws AppException {
        Order order = buildOrder(orderDto);

        log.info("Order: {}", order);
        log.info("Order ITEMS: {}", order.getOrderItems());

        orderRepository.save(order);

        OrderCreatedEvent orderCreatedEvent = orderEventMapper.toOrderCreatedEvent(order);
        OrderCreatedEventDto orderCreatedEventDto = orderEventMapper.toDto(orderCreatedEvent);

        log.info("Order created event DTO: {}", orderCreatedEventDto);

        eventPublisher.publish(new OrderCreated(orderCreatedTopic, orderCreatedEventDto, order.getId().toString()));
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
}

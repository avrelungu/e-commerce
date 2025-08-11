package com.example.order_service.service;

import com.example.order_service.dto.CreateOrderDto;
import com.example.order_service.dto.OrderItemDto;
import com.example.order_service.event.OrderCreated;
import com.example.order_service.exceptions.AppException;
import com.example.order_service.mapper.OrderItemMapper;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;
import com.example.order_service.publisher.EventPublisher;
import com.example.order_service.repository.OrderItemRepository;
import com.example.order_service.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class OrderService {
    private final OrderItemMapper orderItemMapper;

    @Value("${inventory.products.tax-rate}")
    private BigDecimal SALES_TAX_RATE;

    @Value("${order.events.order-created}")
    private String orderCreateTopic;

    private final EventPublisher eventPublisher;

    private final OrderNumberGenerator orderNumberGenerator;

    private final OrderMapper orderMapper;

    private final OrderRepository orderRepository;

    public OrderService(
            OrderMapper orderMapper,
            OrderRepository orderRepository,
            OrderNumberGenerator orderNumberGenerator,
            EventPublisher eventPublisher,
            OrderItemMapper orderItemMapper
    ) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
        this.orderNumberGenerator = orderNumberGenerator;
        this.eventPublisher = eventPublisher;
        this.orderItemMapper = orderItemMapper;
    }

    public void createOrder(CreateOrderDto orderDto) throws AppException {
        Order order = buildOrder(orderDto);

        log.info("Order: {}", order);
        log.info("Order ITEMS: {}", order.getOrderItems());

        orderRepository.save(order);

        eventPublisher.publish(new OrderCreated(orderCreateTopic, orderDto, order.getId().toString()));
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

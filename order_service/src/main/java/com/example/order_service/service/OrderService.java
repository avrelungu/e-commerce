package com.example.order_service.service;

import com.example.order_service.dto.CreateOrderDto;
import com.example.order_service.dto.OrderDto;
import com.example.order_service.dto.OrderItemDto;
import com.example.order_service.dto.OrderItemsAvailableInventoryDto;
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
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {
    private final InventoryService inventoryService;
    private final OrderItemMapper orderItemMapper;
    private final OrderItemRepository orderItemRepository;
    @Value("${inventory.products.tax-rate}")
    private Integer SALES_TAX_RATE;

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
            InventoryService inventoryService,
            OrderItemMapper orderItemMapper, OrderItemRepository orderItemRepository) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
        this.orderNumberGenerator = orderNumberGenerator;
        this.eventPublisher = eventPublisher;
        this.inventoryService = inventoryService;
        this.orderItemMapper = orderItemMapper;
        this.orderItemRepository = orderItemRepository;
    }

    public void createOrder(CreateOrderDto orderDto) throws AppException {
        List<OrderItemsAvailableInventoryDto> orderItemsAvailableInventoryDtos = inventoryService.checkOrderItemsInventory(orderDto);

        assert orderItemsAvailableInventoryDtos != null;

        BigDecimal taxAmount = orderItemsAvailableInventoryDtos.stream()
                .map(inventoryDto -> (inventoryDto.getTotalPrice()
                        .multiply(BigDecimal.valueOf(SALES_TAX_RATE)))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subTotal = orderItemsAvailableInventoryDtos.stream()
                .map(OrderItemsAvailableInventoryDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        Order order = orderMapper.toOrderFromCreateOrderDto(orderDto);
        order.setOrderNumber(orderNumberGenerator.generateOrderNumber());
        order.setTotalAmount(subTotal.add(taxAmount));
        order.setTaxAmount(taxAmount);
        order.setShippingAmount(taxAmount); // TODO: actually calculate the shipping amount

        log.info("Order Items from inventory: {}", orderItemsAvailableInventoryDtos.toString());
        log.info("Order: {}", order);

        orderRepository.save(order);

        List<OrderItem> orderItems = orderItemsAvailableInventoryDtos.stream()
                .map(orderItemMapper::toOrderItem)
                .map(orderItem -> {
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        orderItemRepository::saveAll
                ));

        OrderDto orderDto1 = orderMapper.toOrderDto(order);
        orderDto1.setItems(orderItems.stream()
                .map(orderItemMapper::toOrderItemDto)
                .toList());

        eventPublisher.publish(new OrderCreated(order.getOrderNumber(), orderCreateTopic, orderDto1));
    }
}

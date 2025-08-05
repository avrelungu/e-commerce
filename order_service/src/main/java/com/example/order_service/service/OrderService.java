package com.example.order_service.service;

import com.example.order_service.dto.CreateOrderDto;
import com.example.order_service.dto.OrderDto;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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

        BigDecimal taxAmount = getTaxAmount(orderItemsAvailableInventoryDtos);

        BigDecimal subTotal = getSubTotal(orderItemsAvailableInventoryDtos);

        Order order = buildOrder(orderDto, subTotal, taxAmount);

        log.info("Order Items from inventory: {}", orderItemsAvailableInventoryDtos.toString());
        
        log.info("Order: {}", order);

        orderRepository.save(order);

        List<OrderItem> orderItems= orderItemRepository.saveAll(getOrderItemList(orderItemsAvailableInventoryDtos, order));

        OrderDto orderCreatePayload = buildOrderCreateEventPayload(order, orderItems);

        eventPublisher.publish(new OrderCreated(order.getOrderNumber(), orderCreateTopic, orderCreatePayload));
    }

    private OrderDto buildOrderCreateEventPayload(Order order, List<OrderItem> orderItems) {
        OrderDto orderDto = orderMapper.toOrderDto(order);
        orderDto.setItems(orderItems.stream()
                .map(orderItemMapper::toOrderItemDto)
                .toList());

        return orderDto;
    }

    private List<OrderItem> getOrderItemList(List<OrderItemsAvailableInventoryDto> orderItemsAvailableInventoryDtos, Order order) {
        return orderItemsAvailableInventoryDtos.stream()
                .map(orderItemMapper::toOrderItem)
                .map(orderItem -> {
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .toList();
    }

    private Order buildOrder(CreateOrderDto orderDto, BigDecimal subTotal, BigDecimal taxAmount) {
        Order order = orderMapper.toOrderFromCreateOrderDto(orderDto);
        order.setOrderNumber(orderNumberGenerator.generateOrderNumber());
        order.setTotalAmount(subTotal.add(taxAmount));
        order.setTaxAmount(taxAmount);
        order.setShippingAmount(taxAmount); // TODO: actually calculate the shipping amount

        return order;
    }

    private static BigDecimal getSubTotal(List<OrderItemsAvailableInventoryDto> orderItemsAvailableInventoryDtos) {
        return orderItemsAvailableInventoryDtos.stream()
                .map(OrderItemsAvailableInventoryDto::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTaxAmount(List<OrderItemsAvailableInventoryDto> orderItemsAvailableInventoryDtos) {
        return orderItemsAvailableInventoryDtos.stream()
                .map(inventoryDto -> (inventoryDto.getTotalPrice()
                        .multiply(BigDecimal.valueOf(SALES_TAX_RATE)))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

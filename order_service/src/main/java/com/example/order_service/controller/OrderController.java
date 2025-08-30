package com.example.order_service.controller;

import com.example.order_service.dto.CreateOrderDto;
import com.example.order_service.dto.OrderDto;
import com.example.order_service.dto.OrderItemDto;
import com.example.order_service.exceptions.AppException;
import com.example.order_service.service.OrderItemService;
import com.example.order_service.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;

    public OrderController(OrderService orderService, OrderItemService orderItemService) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody CreateOrderDto orderDto) throws AppException {
        orderService.createOrder(orderDto);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws AppException {
        Page<OrderDto> orders = orderService.getOrders(page, size);

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<List<OrderItemDto>> getAllOrderItems(@PathVariable String orderId) {
        List<OrderItemDto> items = orderItemService.getOrderItems(orderId);

        return ResponseEntity.ok(items);
    }
}

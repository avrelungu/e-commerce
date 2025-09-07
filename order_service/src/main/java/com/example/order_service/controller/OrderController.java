package com.example.order_service.controller;

import com.example.order_service.dto.CreateOrderDto;
import com.example.order_service.dto.OrderDto;
import com.example.order_service.dto.OrderItemDto;
import com.example.order_service.exceptions.AppException;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.Order;
import com.example.order_service.service.OrderItemService;
import com.example.order_service.service.OrderService;
import com.example.shared_common.idempotency.ApiIdempotencyService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ApiIdempotencyService apiIdempotencyService;
    private final OrderMapper orderMapper;

    public OrderController(
            OrderService orderService,
            OrderItemService orderItemService,
            ApiIdempotencyService apiIdempotencyService,
            OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.apiIdempotencyService = apiIdempotencyService;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CreateOrderDto createOrderDto
    ) throws AppException {
        if (idempotencyKey != null) {
            var existingResponse = apiIdempotencyService.checkAndRetrieve(
                idempotencyKey, 
                "order-create", 
                Void.class
            );
            
            if (existingResponse.isPresent()) {
                return ResponseEntity.status(existingResponse.get().getStatusCode()).build();
            }
        }

        Order order = orderService.createOrder(createOrderDto);

        if (idempotencyKey != null) {
            apiIdempotencyService.storeResponse(
                idempotencyKey,
                "order-create", 
                null,
                org.springframework.http.HttpStatus.OK
            );

            OrderDto orderDto = orderMapper.toOrderDto(order);

            return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
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

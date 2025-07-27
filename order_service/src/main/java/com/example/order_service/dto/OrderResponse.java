package com.example.order_service.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private UUID customerId;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private JsonNode shippingAddress;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
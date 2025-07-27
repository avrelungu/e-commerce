package com.example.order_service.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderDto {
    private UUID customerId;
    private List<OrderItemRequest> items;
    private JsonNode shippingAddress;
}
package com.example.inventory_service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OrderItemDto {
    private UUID productId;
    private Integer quantity;
}

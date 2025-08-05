package com.example.inventory_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderItemInventoryCheckDto {
    UUID productId;
    String name;
    BigDecimal price;
    BigDecimal totalPrice;
    Integer availableQuantity;
    Integer reservedQuantity;
    Integer quantity;
    boolean available;
}

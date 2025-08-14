package com.example.order_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemDto {
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("productName")
    private String productName;
    
    @JsonProperty("quantity")
    private int quantity;
    
    @JsonProperty("unitPrice")
    private MoneyDto unitPrice;
    
    @JsonProperty("totalPrice")
    private MoneyDto totalPrice;
}
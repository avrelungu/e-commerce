package com.example.order_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderCreatedEventDto {
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("orderNumber")
    private String orderNumber;
    
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("items")
    private List<OrderItemDto> items;
    
    @JsonProperty("totalAmount")
    private MoneyDto totalAmount;
    
    @JsonProperty("taxAmount")
    private MoneyDto taxAmount;
    
    @JsonProperty("shippingAmount")
    private MoneyDto shippingAmount;
    
    @JsonProperty("shippingAddress")
    private AddressDto shippingAddress;
}
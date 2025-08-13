package com.example.order_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    public OrderItemDto() {}

    public OrderItemDto(String productId, String productName, int quantity, MoneyDto unitPrice, MoneyDto totalPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public MoneyDto getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(MoneyDto unitPrice) {
        this.unitPrice = unitPrice;
    }

    public MoneyDto getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(MoneyDto totalPrice) {
        this.totalPrice = totalPrice;
    }
}
package com.example.order_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class OrderCreatedEventDto {
    @JsonProperty("timestamp")
    private long timestamp;
    
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

    public OrderCreatedEventDto() {}

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemDto> getItems() {
        return items;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items;
    }

    public MoneyDto getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(MoneyDto totalAmount) {
        this.totalAmount = totalAmount;
    }

    public MoneyDto getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(MoneyDto taxAmount) {
        this.taxAmount = taxAmount;
    }

    public MoneyDto getShippingAmount() {
        return shippingAmount;
    }

    public void setShippingAmount(MoneyDto shippingAmount) {
        this.shippingAmount = shippingAmount;
    }

    public AddressDto getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(AddressDto shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
package com.example.order_service.config;

import lombok.Getter;

@Getter
public enum OrderTopic {
    ORDER_EVENTS("order-events"),
    INVENTORY_EVENTS("inventory-events"),
    PAYMENT_EVENTS("payment-events"),
    SHIPPING_EVENTS("shipping-events");

    private final String topicName;

    OrderTopic(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public String toString() {
        return topicName;
    }
}
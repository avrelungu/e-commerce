package com.example.order_service.event;

import com.example.order_service.dto.event.OrderCreatedEventDto;

public class OrderCreated extends DomainEvent {
    private final String orderCreateTopic;

    public OrderCreated(String orderCreateTopic, OrderCreatedEventDto order, String orderId) {
        super(order, orderId);

        this.orderCreateTopic = orderCreateTopic;
    }

    @Override
    public String getEventType() {
        return this.orderCreateTopic;
    }

    @Override
    public String getTopic() {
        return this. orderCreateTopic;
    }
}

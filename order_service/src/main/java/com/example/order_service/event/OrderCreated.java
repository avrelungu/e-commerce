package com.example.order_service.event;

import com.example.events.order.OrderCreatedEvent;

public class OrderCreated extends DomainEvent {
    private final String orderCreateTopic;

    public OrderCreated(String orderCreateTopic, OrderCreatedEvent order, String orderId) {
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

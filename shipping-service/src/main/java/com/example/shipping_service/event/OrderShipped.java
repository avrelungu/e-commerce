package com.example.shipping_service.event;

import com.example.shipping_service.dto.event.OrderShippedEventDto;

public class OrderShipped extends DomainEvent {
    private final String orderShippedTopic;

    public OrderShipped(String orderShippedEventTopic, OrderShippedEventDto payload, String aggregateId) {
        super(payload, aggregateId);
        this.orderShippedTopic = orderShippedEventTopic;
    }

    @Override
    public String getEventType() {
        return "";
    }

    @Override
    public String getTopic() {
        return orderShippedTopic;
    }
}
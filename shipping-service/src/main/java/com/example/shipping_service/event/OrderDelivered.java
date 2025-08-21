package com.example.shipping_service.event;

import com.example.shipping_service.dto.event.OrderDeliveredEventDto;

public class OrderDelivered extends DomainEvent {
    private final String orderDeliveredTopic;

    public OrderDelivered(String orderDeliveredEventTopic, OrderDeliveredEventDto payload, String aggregateId) {
        super(payload, aggregateId);
        this.orderDeliveredTopic = orderDeliveredEventTopic;
    }

    @Override
    public String getEventType() {
        return orderDeliveredTopic;
    }

    @Override
    public String getTopic() {
        return orderDeliveredTopic;
    }
}
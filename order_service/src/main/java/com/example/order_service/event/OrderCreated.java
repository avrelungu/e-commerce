package com.example.order_service.event;

public class OrderCreated extends DomainEvent {

    protected OrderCreated(String aggregateId) {
        super(aggregateId);
    }

    @Override
    public String getEventType() {
        return "";
    }

    @Override
    public String getTopic() {
        return "";
    }
}

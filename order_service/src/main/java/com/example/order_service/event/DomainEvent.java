package com.example.order_service.event;

import java.time.Instant;
import java.util.UUID;

public abstract class DomainEvent {
    public final String eventId = UUID.randomUUID().toString();

    private final Instant occurredOn = Instant.now();
    
    private final String aggregateId;

    protected DomainEvent(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public abstract String getEventType();

    public abstract String getTopic();

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }

    public String getAggregateId() {
        return aggregateId;
    }
}

package com.example.order_service.event;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public abstract class DomainEvent {
    public final String eventId = UUID.randomUUID().toString();

    private final Instant occurredOn = Instant.now();
    
    private final String aggregateId;

    public Object payload;

    protected DomainEvent(String aggregateId, Object payload) {
        this.aggregateId = aggregateId;
        this.payload = payload;
    }

    public abstract String getEventType();

    public abstract String getTopic();
}

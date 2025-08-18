package com.example.payment_service.event;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public abstract class DomainEvent {
    public String eventId;

    public String aggregateId;

    public Object payload;

    private final Instant occurredOn = Instant.now();

    protected DomainEvent(Object payload, String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.payload = payload;
        this.aggregateId = aggregateId;
    }

    public abstract String getEventType();

    public abstract String getTopic();
}

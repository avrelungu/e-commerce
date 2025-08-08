package com.example.order_service.event;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public abstract class DomainEvent {
    public final String eventId = UUID.randomUUID().toString();

    private final Instant occurredOn = Instant.now();

    public Object payload;

    protected DomainEvent(Object payload) {
        this.payload = payload;
    }

    public abstract String getEventType();

    public abstract String getTopic();
}

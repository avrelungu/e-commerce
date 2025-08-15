package com.example.payment_service.event;

import lombok.Data;

import java.time.Instant;

@Data
public abstract class DomainEvent {
    public String eventId;

    public Object payload;

    private final Instant occurredOn = Instant.now();

    protected DomainEvent(Object payload, String eventId) {
        this.payload = payload;
        this.eventId = eventId;
    }

    public abstract String getEventType();

    public abstract String getTopic();
}

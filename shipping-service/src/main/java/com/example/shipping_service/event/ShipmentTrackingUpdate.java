package com.example.shipping_service.event;

import com.example.shipping_service.dto.event.ShipmentTrackingUpdateEventDto;

public class ShipmentTrackingUpdate extends DomainEvent {
    private final String shipmentTrackingUpdateTopic;

    public ShipmentTrackingUpdate(String shipmentTrackingUpdateEventTopic, ShipmentTrackingUpdateEventDto payload, String aggregateId) {
        super(payload, aggregateId);
        this.shipmentTrackingUpdateTopic = shipmentTrackingUpdateEventTopic;
    }

    @Override
    public String getEventType() {
        return shipmentTrackingUpdateTopic;
    }

    @Override
    public String getTopic() {
        return shipmentTrackingUpdateTopic;
    }
}
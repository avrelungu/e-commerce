package com.example.inventory_service.event;

import com.example.events.inventory.OutOfStockEvent;

public class OutOfStock extends DomainEvent {
    private final String insufficientStockTopic;

    public OutOfStock(String insufficientStockTopic, OutOfStockEvent stockReservedEventDto, String orderId) {
        super(stockReservedEventDto, orderId);

        this.insufficientStockTopic = insufficientStockTopic;
    }

    @Override
    public String getEventType() {
        return insufficientStockTopic;
    }

    @Override
    public String getTopic() {
        return insufficientStockTopic;
    }
}

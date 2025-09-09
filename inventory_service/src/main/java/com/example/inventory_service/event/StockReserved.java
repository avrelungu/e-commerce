package com.example.inventory_service.event;

import com.example.events.inventory.StockReservedEvent;

public class StockReserved extends DomainEvent {
    private final String stockReservationTopic;

    public StockReserved(String stockReservationTopic, StockReservedEvent stockReservedEvent, String orderId) {
        super(stockReservedEvent, orderId);

        this.stockReservationTopic = stockReservationTopic;
    }

    @Override
    public String getEventType() {
        return stockReservationTopic;
    }

    @Override
    public String getTopic() {
        return stockReservationTopic;
    }
}

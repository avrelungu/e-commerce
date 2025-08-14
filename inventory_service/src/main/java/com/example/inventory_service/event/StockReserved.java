package com.example.inventory_service.event;

import com.example.inventory_service.dto.event.StockReservedEventDto;

public class StockReserved extends DomainEvent {
    private final String stockReservationTopic;

    public StockReserved(String stockReservationTopic, StockReservedEventDto stockReservedEventDto, String orderId) {
        super(stockReservedEventDto, orderId);

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

package com.example.inventory_service.event;

import com.example.inventory_service.dto.event.OutOfStockEventDto;

public class OutOfStock extends DomainEvent {
    private final String insufficientStockTopic;

    public OutOfStock(String insufficientStockTopic, OutOfStockEventDto stockReservedEventDto, String orderId) {
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

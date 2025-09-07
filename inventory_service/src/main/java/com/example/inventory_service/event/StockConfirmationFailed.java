package com.example.inventory_service.event;

public class StockConfirmationFailed extends DomainEvent {
    private final String stockConfirmationFailedTopic;

    public StockConfirmationFailed(String topic, Object payload, String aggregateId) {
        super(payload, aggregateId);

        stockConfirmationFailedTopic = topic;
    }

    @Override
    public String getEventType() {
        return stockConfirmationFailedTopic;
    }

    @Override
    public String getTopic() {
        return stockConfirmationFailedTopic;
    }
}

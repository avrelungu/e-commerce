package com.example.payment_service.event;

public class RefundRequestProcessed extends DomainEvent {
    private String refundProcessedTopic;
    
    public RefundRequestProcessed(String refundProcessedTopic, Object payload, String orderId) {
        super(payload, orderId);
        this.refundProcessedTopic = refundProcessedTopic;
    }

    @Override
    public String getEventType() {
        return "RefundRequestProcessed";
    }

    @Override
    public String getTopic() {
        return refundProcessedTopic;
    }
}
package com.example.payment_service.event;

public class RefundRequestFailed extends DomainEvent {
    private String refundFailedTopic;
    
    public RefundRequestFailed(String refundFailedTopic, Object payload, String orderId) {
        super(payload, orderId);
        this.refundFailedTopic = refundFailedTopic;
    }

    @Override
    public String getEventType() {
        return "RefundRequestFailed";
    }

    @Override
    public String getTopic() {
        return refundFailedTopic;
    }
}
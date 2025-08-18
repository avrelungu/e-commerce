package com.example.payment_service.event;

public class PaymentRequestFailed extends DomainEvent {
    private String paymentFailedTopic;

    public PaymentRequestFailed(String paymentFailedTopic, String orderId, Object payload) {
        super(payload, orderId);
        this.paymentFailedTopic = paymentFailedTopic;
    }

    @Override
    public String getEventType() {
        return "PaymentRequestFailed";
    }

    @Override
    public String getTopic() {
        return paymentFailedTopic;
    }
}
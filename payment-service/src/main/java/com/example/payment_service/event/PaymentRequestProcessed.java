package com.example.payment_service.event;

public class PaymentRequestProcessed extends DomainEvent {
    private String paymentProcessedTopic;

    public PaymentRequestProcessed(String paymentProcessedTopic, String orderId, Object payload) {
        super(payload, orderId);
        this.paymentProcessedTopic = paymentProcessedTopic;
    }

    @Override
    public String getEventType() {
        return "PaymentRequestProcessed";
    }

    @Override
    public String getTopic() {
        return paymentProcessedTopic;
    }
}

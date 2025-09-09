package com.example.payment_service.event;

import com.example.events.payment.PaymentFailedEvent;

public class PaymentRequestFailed extends DomainEvent {
    private final String paymentFailedTopic;

    public PaymentRequestFailed(String paymentFailedTopic, PaymentFailedEvent payload, String orderId) {
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
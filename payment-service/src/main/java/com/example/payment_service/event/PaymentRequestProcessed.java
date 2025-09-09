package com.example.payment_service.event;

import com.example.events.payment.PaymentProcessedEvent;

public class PaymentRequestProcessed extends DomainEvent {
    private final String paymentProcessedTopic;

    public PaymentRequestProcessed(String paymentProcessedTopic, PaymentProcessedEvent paymentProcessedEvent,  String orderId) {
        super(paymentProcessedEvent, orderId);
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

package com.example.order_service.event;

import com.example.events.payment.PaymentRequestEvent;

public class PaymentRequest extends DomainEvent {
    private final String paymentRequestTopic;

    public PaymentRequest(String paymentRequestEventTopic, PaymentRequestEvent paymentRequestEventDto, String orderId) {
        super(paymentRequestEventDto, orderId);

        paymentRequestTopic = paymentRequestEventTopic;
    }

    @Override
    public String getEventType() {
        return paymentRequestTopic;
    }

    @Override
    public String getTopic() {
        return paymentRequestTopic;
    }
}

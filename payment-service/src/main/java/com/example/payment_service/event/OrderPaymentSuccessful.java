package com.example.payment_service.event;

public class OrderPaymentSuccessful extends DomainEvent {
    private String orderPaymentSuccessfulTopic;

    protected OrderPaymentSuccessful(String orderPaymentSuccessfulTopic, Object payload, String eventId) {
        super(payload, eventId);

        this.orderPaymentSuccessfulTopic = orderPaymentSuccessfulTopic;
    }

    @Override
    public String getEventType() {
        return orderPaymentSuccessfulTopic;
    }

    @Override
    public String getTopic() {
        return orderPaymentSuccessfulTopic;
    }
}

package com.example.order_service.dto.event;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PaymentRequestEventDto {
    private String orderId;
    private MoneyDto amount;
    private String paymentMethodToken;
    private String paymentMethodType;
}

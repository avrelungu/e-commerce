package com.example.payment_service.dto.event;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentFailedEventDto {
    private String orderId;
    private BigDecimal amount;
    private String failureReason;
    private int attemptNumber;
    private int maxAttempts;
    private boolean canRetry;
}

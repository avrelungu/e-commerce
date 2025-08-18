package com.example.payment_service.dto.event;

import com.example.events.common.Money;
import com.example.events.payment.PaymentMethod;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentProcessedEventDto {
    private String paymentId;
    private String orderId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId;
    private String lastFourDigits;
    private String cardBrand;
}

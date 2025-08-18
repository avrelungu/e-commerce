package com.example.payment_service.mapper;

import com.example.events.common.Money;
import com.example.events.payment.PaymentFailedEvent;
import com.example.events.payment.PaymentProcessedEvent;
import com.example.payment_service.dto.event.PaymentFailedEventDto;
import com.example.payment_service.dto.event.PaymentProcessedEventDto;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface PaymentRequestMapper {

    PaymentProcessedEventDto toPaymentProcessedEventDto(PaymentProcessedEvent paymentProcessedEvent);

    PaymentFailedEventDto toPaymentFailedEventDto(PaymentFailedEvent paymentFailedEvent);

    default BigDecimal map(Money value) {
        return value.getAmount();
    }
}

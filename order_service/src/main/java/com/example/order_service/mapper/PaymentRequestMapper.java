package com.example.order_service.mapper;

import com.example.events.common.Money;
import com.example.events.payment.PaymentRequestEvent;
import com.example.order_service.dto.event.MoneyDto;
import com.example.order_service.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(componentModel = "spring")
public interface PaymentRequestMapper {
    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "amount", source = "totalAmount")
    PaymentRequestEvent toPaymentRequestEvent(Order order);

    default Money map(BigDecimal value) {
        return Money.newBuilder()
                .setAmount(value.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    default MoneyDto map(Money value) {
        return new MoneyDto(
                value.getAmount(),
                value.getCurrency()
        );
    }
}

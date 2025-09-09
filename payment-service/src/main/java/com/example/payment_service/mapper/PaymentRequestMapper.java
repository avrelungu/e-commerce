package com.example.payment_service.mapper;

import com.example.events.common.Money;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface PaymentRequestMapper {
    default BigDecimal map(Money value) {
        return value.getAmount();
    }
}

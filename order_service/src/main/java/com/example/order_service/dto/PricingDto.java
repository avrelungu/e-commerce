package com.example.order_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PricingDto {
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal subTotal;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal taxAmount;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal totalAmount;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal shippingAmount;
}

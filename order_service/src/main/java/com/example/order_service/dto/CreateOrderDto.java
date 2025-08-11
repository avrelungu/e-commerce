package com.example.order_service.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderDto {
    @NotNull
    private UUID customerId;

    @NotBlank
    @Email
    private String customerEmail;

    @NotBlank
    private String customerName;

    @NotEmpty
    private List<OrderItemDto> items;

    @NotNull
    private AddressDto shippingAddress;

    private AddressDto billingAddress;

    @NotEmpty
    private PaymentMethodDto paymentMethod;

    @NotEmpty
    private PricingDto pricing;
}
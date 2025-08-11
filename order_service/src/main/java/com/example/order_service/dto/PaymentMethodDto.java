package com.example.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PaymentMethodDto {
    @NotBlank
    private String type;

    @Pattern(regexp = "\\d{4}")
    private String last4;

    private String brand;
}

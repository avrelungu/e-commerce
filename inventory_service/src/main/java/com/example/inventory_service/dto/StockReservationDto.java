package com.example.inventory_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class StockReservationDto {
    @JsonProperty("productId")
    private UUID productId;

    @JsonProperty("reservationId")
    private UUID reservationId;

    @JsonProperty("quantity")
    private int quantity;

    @JsonProperty("expiresAt")
    private long expiresAt;
}

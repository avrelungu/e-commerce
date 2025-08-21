package com.example.inventory_service.dto.event;

import com.example.inventory_service.dto.StockReservationDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OutOfStockEventDto {
    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("reservations")
    private List<StockReservationDto> reservations;
}

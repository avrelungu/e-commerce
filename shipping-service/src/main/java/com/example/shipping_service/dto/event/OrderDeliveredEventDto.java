package com.example.shipping_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveredEventDto {

    @JsonProperty("shipmentId")
    private UUID shipmentId;

    @JsonProperty("orderId")
    private UUID orderId;

    @JsonProperty("trackingNumber")
    private String trackingNumber;

    @JsonProperty("deliveredAt")
    private LocalDateTime deliveredAt;

    @JsonProperty("deliveryLocation")
    private String deliveryLocation;
}
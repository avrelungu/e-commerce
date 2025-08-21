package com.example.shipping_service.dto.event;

import com.example.shipping_service.model.Shipment;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentTrackingUpdateEventDto {

    @JsonProperty("shipmentId")
    private UUID shipmentId;

    @JsonProperty("orderId")
    private UUID orderId;

    @JsonProperty("trackingNumber")
    private String trackingNumber;

    @JsonProperty("status")
    private Shipment.ShipmentStatus status;

    @JsonProperty("location")
    private String location;

    @JsonProperty("description")
    private String description;

    @JsonProperty("eventTime")
    private LocalDateTime eventTime;
}
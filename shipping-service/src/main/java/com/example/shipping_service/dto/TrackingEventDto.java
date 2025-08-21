package com.example.shipping_service.dto;

import com.example.shipping_service.model.Shipment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TrackingEventDto {
    private UUID id;
    private UUID shipmentId;
    private Shipment.ShipmentStatus status;
    private String location;
    private String description;
    private LocalDateTime eventTime;
    private LocalDateTime createdAt;
}
package com.example.shipping_service.dto;

import com.example.shipping_service.model.Shipment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ShipmentDto {
    private UUID id;
    private UUID orderId;
    private String trackingNumber;
    private String carrier;
    private Shipment.ShipmentStatus status;
    private String shippingMethod;
    private BigDecimal shippingCost;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    private String labelUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
package com.example.shipping_service.model;

public enum ShipmentStatus {
    PENDING,
    LABEL_GENERATED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    RETURNED
}
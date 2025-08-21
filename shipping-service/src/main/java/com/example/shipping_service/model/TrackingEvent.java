package com.example.shipping_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "tracking_events", schema = "shipping_service")
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "shipment_id", nullable = false)
    private UUID shipmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Shipment.ShipmentStatus status;

    @Column(name = "location")
    private String location;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (eventTime == null) {
            eventTime = LocalDateTime.now();
        }
    }
}
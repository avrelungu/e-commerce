package com.example.order_service.model;

import com.example.order_service.converter.JsonNodeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnTransformer;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "order_events", schema = "order_service")
public class OrderEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "event_type", length = 50)
    private String eventType;

    @Convert(converter = JsonNodeConverter.class)
    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "event_payload", columnDefinition = "jsonb")
    private JsonNode eventPayload;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
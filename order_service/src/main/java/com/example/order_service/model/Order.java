package com.example.order_service.model;

import com.example.order_service.converter.JsonNodeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnTransformer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "orders", schema = "order_service")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, name = "order_number")
    private String orderNumber;

    @Column(nullable = false, name = "customer_id")
    private UUID customerId;

    @Column(nullable = false, name = "status")
    private String status = "PENDING";

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "shipping_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingAmount;

    @Convert(converter = JsonNodeConverter.class)
    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "shipping_address", nullable = false, columnDefinition = "jsonb")
    private JsonNode shippingAddress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}

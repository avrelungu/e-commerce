package com.example.shipping_service.model;

import com.example.shipping_service.converter.JsonNodeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnTransformer;

import java.util.UUID;

@Data
@Entity
@Table(name = "shipping_addresses", schema = "shipping_service")
public class ShippingAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "shipment_id", nullable = false)
    private UUID shipmentId;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "address_data", columnDefinition = "jsonb")
    @Convert(converter = JsonNodeConverter.class)
    @ColumnTransformer(write = "?::jsonb")
    private JsonNode addressData;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;
}
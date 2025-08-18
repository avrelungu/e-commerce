package com.example.payment_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "payment_methods", schema = "payment_service")
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PaymentMethodType type;

    @Column(name = "token", nullable = false, length = 255)
    private String token; // Encrypted payment token

    @Column(name = "last_four", length = 4)
    private String lastFour;

    @Column(name = "brand", length = 20)
    private String brand; // VISA, MASTERCARD, etc.

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PaymentMethodType {
        CARD, WALLET
    }
}
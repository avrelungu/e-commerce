package com.example.order_service.model;

import com.example.order_service.converter.JsonNodeConverter;
import com.example.order_service.enums.OrderStatus;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.ColumnTransformer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@ToString(exclude = {"orderItems"})
@Entity
@Table(name = "orders", schema = "order_service")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, name = "order_number")
    private String orderNumber;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "status")
    private String status = String.valueOf(OrderStatus.PENDING);

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "shipping_amount", precision = 10, scale = 2)
    private BigDecimal shippingAmount;

    @Convert(converter = JsonNodeConverter.class)
    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "shipping_address", columnDefinition = "jsonb")
    private JsonNode shippingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Temporary payment fields (cleared after payment processing)
    @Column(name = "payment_method_token", nullable = true)
    private String paymentMethodToken;

    @Column(name = "payment_method_type", nullable = true)
    private String paymentMethodType;

    // Permanent payment fields (kept for records)
    @Column(name = "payment_status", nullable = true)
    private String paymentStatus;

    @Column(name = "transaction_id", nullable = true)
    private String transactionId;

    @Column(name = "last_four_digits", nullable = true)
    private String lastFourDigits;
}

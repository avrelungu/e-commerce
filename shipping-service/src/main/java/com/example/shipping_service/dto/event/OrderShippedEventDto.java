package com.example.shipping_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderShippedEventDto {

    @JsonProperty("shipmentId")
    private UUID shipmentId;

    @JsonProperty("orderId")
    private UUID orderId;

    @JsonProperty("trackingNumber")
    private String trackingNumber;

    @JsonProperty("carrier")
    private String carrier;

    @JsonProperty("shippingMethod")
    private String shippingMethod;

    @JsonProperty("estimatedDelivery")
    private LocalDateTime estimatedDelivery;

    @JsonProperty("labelUrl")
    private String labelUrl;

    @JsonProperty("shippedAt")
    private LocalDateTime shippedAt;
}
package com.example.shipping_service.dto;

import lombok.Data;

@Data
public class TrackingUpdateDto {
    private String trackingNumber;
    private String location;
    private String description;
}
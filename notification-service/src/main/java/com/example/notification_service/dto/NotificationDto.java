package com.example.notification_service.dto;

import com.example.notification_service.model.NotificationStatus;
import com.example.notification_service.model.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationDto {
    private UUID id;
    private UUID orderId;
    private String customerEmail;
    private String customerPhone;
    private NotificationType type;
    private String subject;
    private String message;
    private NotificationStatus status;
    private String eventType;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
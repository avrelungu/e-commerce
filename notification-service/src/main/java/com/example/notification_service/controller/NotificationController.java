package com.example.notification_service.controller;

import com.example.notification_service.dto.NotificationDto;
import com.example.notification_service.model.Notification;
import com.example.notification_service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<List<NotificationDto>> getNotificationHistory(@PathVariable UUID orderId) {
        log.info("Getting notification history for order: {}", orderId);
        
        List<Notification> notifications = notificationService.getNotificationHistory(orderId);
        List<NotificationDto> notificationDtos = notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(notificationDtos);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<NotificationDto>> getPendingNotifications() {
        log.info("Getting pending notifications");
        
        List<Notification> notifications = notificationService.getPendingNotifications();
        List<NotificationDto> notificationDtos = notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(notificationDtos);
    }

    @PostMapping("/retry/{notificationId}")
    public ResponseEntity<Void> retryNotification(@PathVariable UUID notificationId) {
        log.info("Manual retry requested for notification: {}", notificationId);
        
        // In a real implementation, you'd fetch the notification and retry it
        // For now, we'll just return success
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Notification service is healthy");
    }

    private NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setOrderId(notification.getOrderId());
        dto.setCustomerEmail(notification.getCustomerEmail());
        dto.setCustomerPhone(notification.getCustomerPhone());
        dto.setType(notification.getType());
        dto.setSubject(notification.getSubject());
        dto.setMessage(notification.getMessage());
        dto.setStatus(notification.getStatus());
        dto.setEventType(notification.getEventType());
        dto.setSentAt(notification.getSentAt());
        dto.setDeliveredAt(notification.getDeliveredAt());
        dto.setErrorMessage(notification.getErrorMessage());
        dto.setRetryCount(notification.getRetryCount());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setUpdatedAt(notification.getUpdatedAt());
        return dto;
    }
}
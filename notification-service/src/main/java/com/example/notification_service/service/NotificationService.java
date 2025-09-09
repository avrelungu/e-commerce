package com.example.notification_service.service;

import com.example.notification_service.model.Notification;
import com.example.notification_service.model.NotificationStatus;
import com.example.notification_service.model.NotificationTemplate;
import com.example.notification_service.model.NotificationType;
import com.example.notification_service.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TemplateService templateService;
    private final EmailService emailService;
    private final SmsService smsService;

    public NotificationService(NotificationRepository notificationRepository,
                             TemplateService templateService,
                             EmailService emailService,
                             SmsService smsService) {
        this.notificationRepository = notificationRepository;
        this.templateService = templateService;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    @Async
    public void processOrderEvent(String eventType, UUID orderId, Map<String, String> eventData) {
        log.info("Processing {} event for order: {}", eventType, orderId);

        // Extract customer info - in production, this would come from the event or be fetched from order service
        String customerEmail = eventData.getOrDefault("customerEmail", "customer@example.com");
        String customerPhone = eventData.getOrDefault("customerPhone", "+1-555-0123");

        // Send email notification
        sendEmailNotification(eventType, orderId, customerEmail, eventData);

        // Send SMS notification (for important events)
        if (shouldSendSms(eventType)) {
            sendSmsNotification(eventType, orderId, customerPhone, eventData);
        }
    }

    private void sendEmailNotification(String eventType, UUID orderId, String customerEmail, Map<String, String> variables) {
        Optional<NotificationTemplate> templateOpt = templateService.findTemplate(eventType, NotificationType.EMAIL);

        if (templateOpt.isEmpty()) {
            log.warn("No email template found for event type: {}", eventType);
            return;
        }

        NotificationTemplate template = templateOpt.get();
        String subject = templateService.processTemplate(template.getSubject(), variables);
        String message = templateService.processTemplate(template.getMessageTemplate(), variables);

        // Create notification record
        Notification notification = Notification.builder()
                .orderId(orderId)
                .customerEmail(customerEmail)
                .type(NotificationType.EMAIL)
                .subject(subject)
                .message(message)
                .status(NotificationStatus.PENDING)
                .eventType(eventType)
                .build();

        notification = notificationRepository.save(notification);

        // Send email asynchronously
        sendEmailAsync(notification);
    }

    private void sendSmsNotification(String eventType, UUID orderId, String customerPhone, Map<String, String> variables) {
        Optional<NotificationTemplate> templateOpt = templateService.findTemplate(eventType, NotificationType.SMS);

        if (templateOpt.isEmpty()) {
            log.warn("No SMS template found for event type: {}", eventType);
            return;
        }

        NotificationTemplate template = templateOpt.get();
        String message = templateService.processTemplate(template.getMessageTemplate(), variables);

        // Create notification record
        Notification notification = Notification.builder()
                .orderId(orderId)
                .customerPhone(customerPhone)
                .customerEmail("sms@example.com") // Required field, use placeholder
                .type(NotificationType.SMS)
                .message(message)
                .status(NotificationStatus.PENDING)
                .eventType(eventType)
                .build();

        notification = notificationRepository.save(notification);

        // Send SMS asynchronously
        sendSmsAsync(notification);
    }

    @Async
    public void sendEmailAsync(Notification notification) {
        try {
            boolean success = emailService.sendEmail(
                    notification.getCustomerEmail(), 
                    notification.getSubject(), 
                    notification.getMessage()
            );

            if (success) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                log.info("Email notification sent successfully: {}", notification.getId());
            } else {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setErrorMessage("Failed to send email");
                log.error("Failed to send email notification: {}", notification.getId());
            }

            notificationRepository.save(notification);

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);
            log.error("Error sending email notification: {}", notification.getId(), e);
        }
    }

    @Async
    public void sendSmsAsync(Notification notification) {
        try {
            boolean success = smsService.sendSms(
                    notification.getCustomerPhone(), 
                    notification.getMessage()
            );

            if (success) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                log.info("SMS notification sent successfully: {}", notification.getId());
            } else {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setErrorMessage("Failed to send SMS");
                log.error("Failed to send SMS notification: {}", notification.getId());
            }

            notificationRepository.save(notification);

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);
            log.error("Error sending SMS notification: {}", notification.getId(), e);
        }
    }

    // Retry failed notifications periodically
//    @Scheduled(fixedDelay = 60000)
    public void retryFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository.findFailedNotificationsForRetry();

        for (Notification notification : failedNotifications) {
            log.info("Retrying failed notification: {} (attempt {})", 
                    notification.getId(), notification.getRetryCount() + 1);

            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setStatus(NotificationStatus.PENDING);
            notificationRepository.save(notification);

            if (notification.getType() == NotificationType.EMAIL) {
                sendEmailAsync(notification);
            } else {
                sendSmsAsync(notification);
            }
        }
    }

    public List<Notification> getNotificationHistory(UUID orderId) {
        return notificationRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }

    public List<Notification> getPendingNotifications() {
        return notificationRepository.findByStatus(NotificationStatus.PENDING);
    }

    private boolean shouldSendSms(String eventType) {
        // Only send SMS for critical events to avoid spamming
        return eventType.equals("order-created") || 
               eventType.equals("shipping-order-shipped") || 
               eventType.equals("shipping-order-delivered");
    }
}
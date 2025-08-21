package com.example.notification_service.service;

import com.example.notification_service.model.NotificationTemplate;
import com.example.notification_service.model.NotificationType;
import com.example.notification_service.repository.NotificationTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final Pattern placeholderPattern = Pattern.compile("\\{\\{(.*?)\\}\\}");

    public TemplateService(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public Optional<NotificationTemplate> findTemplate(String eventType, NotificationType type) {
        return templateRepository.findByEventTypeAndTypeAndActive(eventType, type, true);
    }

    public String processTemplate(String template, Map<String, String> variables) {
        if (template == null || variables == null) {
            return template;
        }

        String processedTemplate = template;
        Matcher matcher = placeholderPattern.matcher(template);

        while (matcher.find()) {
            String placeholder = matcher.group(0); // {{variableName}}
            String variableName = matcher.group(1); // variableName
            String value = variables.getOrDefault(variableName, placeholder);
            processedTemplate = processedTemplate.replace(placeholder, value);
        }

        return processedTemplate;
    }

    public void initializeDefaultTemplates() {
        log.info("Initializing default notification templates...");

        // Order Created Templates
        createTemplateIfNotExists("order-created-email", NotificationType.EMAIL, "order-created",
                "Order Confirmation - {{orderNumber}}",
                "Thank you for your order!\n\n" +
                "Order Number: {{orderNumber}}\n" +
                "Total Amount: {{totalAmount}}\n" +
                "Items: {{itemCount}} items\n\n" +
                "We'll send you updates as your order progresses.\n\n" +
                "Thank you for shopping with us!");

        createTemplateIfNotExists("order-created-sms", NotificationType.SMS, "order-created",
                null,
                "Order {{orderNumber}} confirmed! Total: {{totalAmount}}. We'll keep you updated.");

        // Payment Processed Templates
        createTemplateIfNotExists("payment-processed-email", NotificationType.EMAIL, "payment-processed",
                "Payment Confirmed - Order {{orderNumber}}",
                "Great news! Your payment has been processed.\n\n" +
                "Order Number: {{orderNumber}}\n" +
                "Amount Paid: {{amount}}\n" +
                "Payment Method: {{paymentMethod}}\n\n" +
                "Your order is now being prepared for shipment.\n\n" +
                "Thank you!");

        createTemplateIfNotExists("payment-processed-sms", NotificationType.SMS, "payment-processed",
                null,
                "Payment of {{amount}} confirmed for order {{orderNumber}}. Preparing for shipment!");

        // Order Shipped Templates
        createTemplateIfNotExists("order-shipped-email", NotificationType.EMAIL, "shipping-order-shipped",
                "Your Order Has Shipped - {{trackingNumber}}",
                "Exciting news! Your order is on its way.\n\n" +
                "Order Number: {{orderNumber}}\n" +
                "Tracking Number: {{trackingNumber}}\n" +
                "Carrier: {{carrier}}\n" +
                "Estimated Delivery: {{estimatedDelivery}}\n\n" +
                "Track your package at: {{trackingUrl}}\n\n" +
                "Thank you for your business!");

        createTemplateIfNotExists("order-shipped-sms", NotificationType.SMS, "shipping-order-shipped",
                null,
                "Order {{orderNumber}} shipped! Track: {{trackingNumber}} via {{carrier}}. ETA: {{estimatedDelivery}}");

        // Order Delivered Templates
        createTemplateIfNotExists("order-delivered-email", NotificationType.EMAIL, "shipping-order-delivered",
                "Package Delivered - Order {{orderNumber}}",
                "Your order has been delivered!\n\n" +
                "Order Number: {{orderNumber}}\n" +
                "Tracking Number: {{trackingNumber}}\n" +
                "Delivered At: {{deliveredAt}}\n" +
                "Location: {{deliveryLocation}}\n\n" +
                "We hope you enjoy your purchase. Please let us know if you have any issues!\n\n" +
                "Thank you for choosing us!");

        createTemplateIfNotExists("order-delivered-sms", NotificationType.SMS, "shipping-order-delivered",
                null,
                "Order {{orderNumber}} delivered to {{deliveryLocation}}! Thank you for your business.");

        log.info("Default notification templates initialized successfully");
    }

    private void createTemplateIfNotExists(String templateName, NotificationType type, String eventType, 
                                         String subject, String messageTemplate) {
        Optional<NotificationTemplate> existing = templateRepository.findByTemplateNameAndActive(templateName, true);
        
        if (existing.isEmpty()) {
            NotificationTemplate template = NotificationTemplate.builder()
                    .templateName(templateName)
                    .type(type)
                    .eventType(eventType)
                    .subject(subject)
                    .messageTemplate(messageTemplate)
                    .active(true)
                    .build();
            
            templateRepository.save(template);
            log.info("Created template: {}", templateName);
        }
    }
}
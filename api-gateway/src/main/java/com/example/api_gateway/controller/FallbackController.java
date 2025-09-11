package com.example.api_gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping("/orders")
    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> orderServiceFallback() {
        return createFallbackResponse("Order Service", 
            "Order service is temporarily unavailable. Please try again later.");
    }
    
    @GetMapping("/payments")
    @PostMapping("/payments")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        return createFallbackResponse("Payment Service", 
            "Payment service is temporarily unavailable. Your order is safe and will be processed once the service is restored.");
    }
    
    @GetMapping("/inventory")
    @PostMapping("/inventory")
    public ResponseEntity<Map<String, Object>> inventoryServiceFallback() {
        return createFallbackResponse("Inventory Service", 
            "Inventory service is temporarily unavailable. Product information may be outdated.");
    }
    
    @GetMapping("/shipping")
    @PostMapping("/shipping")
    public ResponseEntity<Map<String, Object>> shippingServiceFallback() {
        return createFallbackResponse("Shipping Service", 
            "Shipping service is temporarily unavailable. Tracking information may be delayed.");
    }
    
    @GetMapping("/notifications")
    @PostMapping("/notifications")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        return createFallbackResponse("Notification Service", 
            "Notification service is temporarily unavailable. You may not receive updates, but your orders are being processed.");
    }
    
    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("service", serviceName);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        response.put("suggestion", "Please try again in a few moments or contact support if the issue persists.");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
package com.example.order_service.service;

import org.springframework.stereotype.Component;

@Component
public class OrderNumberGenerator {
    public String generateOrderNumber() {
        return System.currentTimeMillis() + "";
    }
}

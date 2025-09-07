package com.example.inventory_service.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends AppException {
    public InsufficientStockException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}

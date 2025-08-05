package com.example.order_service.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class AppException extends Throwable {
    @Getter
    private final HttpStatus httpStatus;

    public AppException(String message, HttpStatus httpStatus) {
        super(message);

        this.httpStatus = httpStatus;
    }
}

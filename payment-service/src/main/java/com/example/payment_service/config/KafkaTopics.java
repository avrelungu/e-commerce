package com.example.payment_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "kafka.topics")
@Component
@Data
public class KafkaTopics {
    private String orderCreated;
    private String stockReserved;
    private String stockReleased;
    private String outOfStock;
    private String paymentRequest;
}

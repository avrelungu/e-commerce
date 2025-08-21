package com.example.shipping_service.config;

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
    private String paymentProcessed;
    private String paymentFailed;
    private String orderShipped;
    private String orderDelivered;
    private String shipmentTrackingUpdate;
}

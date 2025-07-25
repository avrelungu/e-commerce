package com.example.inventory_service.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    @KafkaListener(
            topics = "orders",
            groupId = "inventory-service-group"
    )
    public void orderConsumer(String event) {
        System.out.println(event + " recevied and consumed");
    }
}

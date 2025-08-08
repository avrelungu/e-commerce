package com.example.inventory_service.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventConsumer {

    @KafkaListener(topics = "${order.events.order-created}")
    public void orderCreateListener(String event) {
        log.info("Order created from inventory: {}: ", event);
    }
}

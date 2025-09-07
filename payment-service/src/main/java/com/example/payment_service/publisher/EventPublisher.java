package com.example.payment_service.publisher;

import com.example.events.payment.PaymentFailedEvent;
import com.example.events.payment.PaymentProcessedEvent;
import com.example.payment_service.dto.event.PaymentProcessedEventDto;
import com.example.payment_service.event.DomainEvent;
import com.example.payment_service.event.PaymentRequestProcessed;
import com.example.payment_service.mapper.PaymentRequestMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper,
            PaymentRequestMapper paymentRequestMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(DomainEvent event) {
        try {
            log.info("Publishing event: {}, {}", event, event.getTopic());

            kafkaTemplate.send(event.getTopic(), event.getPartitionKey() ,event.getPayload())
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            log.error("Error publishing event", exception);
                        } else {
                            log.info("Published event {}", event);
                        }
                    });
        } catch (Exception e) {
            log.error(
                    "And error occurred when sending message {} to topic {}: {}",
                    event.getEventId(),
                    event.getTopic(),
                    e.getMessage()
            );
        }
    }
}

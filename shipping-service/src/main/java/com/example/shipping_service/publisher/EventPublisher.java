package com.example.shipping_service.publisher;

import com.example.shipping_service.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(DomainEvent event) {
        try {
            String domainEvent = objectMapper.writeValueAsString(event);

            log.info("Publishing event: {}, {}, {}", event.getEventType(), domainEvent, event.getTopic());

            kafkaTemplate.send(event.getTopic(), event.getPartitionKey() , domainEvent)
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
package com.example.order_service.publisher;

import com.example.order_service.event.DomainEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<Object, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EventPublisher(
            KafkaTemplate<Object, String> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(DomainEvent event) {
        log.info("Publishing event: {}, {}, {}", event.getEventType(), event.getEventId(), event.getTopic());

        try {
            String domainEvent = objectMapper.writeValueAsString(event);

            log.info("Domain event: {}", domainEvent);

            kafkaTemplate.send(event.getTopic(), event.getEventId() , domainEvent)
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

package com.example.order_service.publisher;

import com.example.order_service.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<Object, String> kafkaTemplate;

    public EventPublisher(KafkaTemplate<Object, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(DomainEvent event) {
        log.info("Publishing event: {}, {}, {}", event.getEventType(), event.getEventId(), event.getTopic());

        try {
            kafkaTemplate.send(event.getTopic(), event.toString())
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

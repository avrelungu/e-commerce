package com.example.order_service.publisher;

import com.example.events.order.OrderCreatedEvent;
import com.example.order_service.event.DomainEvent;
import com.example.order_service.event.OrderCreated;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.*;

@SpringBootTest
public class EventPublisherRetryTest {

    @MockitoBean
    private KafkaTemplate<Object, String> kafkaTemplate;

    @Autowired
    private EventPublisher eventPublisher;

    @Test
    void shouldPublishEvent() throws Exception {
        DomainEvent event = new OrderCreated("123", new OrderCreatedEvent(), "123");

        eventPublisher.publish(event);

        verify(kafkaTemplate, times(1))
                .send(eq(event.getTopic()), eq(event.getPartitionKey()), anyString());
    }
}

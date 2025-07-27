package com.example.order_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@EnableKafka
@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(OrderTopic.ORDER_EVENTS.getTopicName())
                .partitions(3)
                .replicas(1)
                .build();
    }
}
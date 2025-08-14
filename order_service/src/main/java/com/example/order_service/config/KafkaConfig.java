package com.example.order_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.web.client.RestTemplate;

@EnableKafka
@Configuration
public class KafkaConfig {
    @Value("#{kafkaTopics.orderCreated}")
    private String orderCreatedTopic;

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(orderCreatedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
package com.example.inventory_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Value("#{kafkaTopics.stockReserved}")
    private String stockReservationTopic;

    @Bean
    public NewTopic stockReservationTopic() {
        return TopicBuilder.name(stockReservationTopic)
                .partitions(3)
                .replicas(1)
                .build();
    };
}

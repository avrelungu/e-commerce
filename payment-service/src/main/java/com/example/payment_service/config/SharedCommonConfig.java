package com.example.payment_service.config;

import com.example.shared_common.idempotency.ApiIdempotencyService;
import com.example.shared_common.idempotency.EventIdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class SharedCommonConfig {
    @Bean
    public ApiIdempotencyService apiIdempotencyService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper
    ) {
        return new ApiIdempotencyService(redisTemplate, objectMapper);
    }

    @Bean
    public EventIdempotencyService eventIdempotencyService(
            RedisTemplate<String, String> redisTemplate,
            @Value("${spring.application.name}") String serviceName
    ) {
        return new EventIdempotencyService(redisTemplate, serviceName);
    }
}

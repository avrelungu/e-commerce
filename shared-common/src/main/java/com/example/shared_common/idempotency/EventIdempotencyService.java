package com.example.shared_common.idempotency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
public class EventIdempotencyService {
    private final RedisTemplate<String, String> redisTemplate;
    private final String serviceName;
    private static final Duration EVENT_TTL = Duration.ofDays(7);
    private static final Duration PROCESSING_LOCK_TTL = Duration.ofMinutes(5);

    public EventIdempotencyService(
            RedisTemplate<String, String> redisTemplate,
            @Value("${spring.application.name:unknown}") String serviceName) {
        this.redisTemplate = redisTemplate;
        this.serviceName = serviceName;
    }

    public boolean isAlreadyProcessed(String eventId) {
        String key = buildProcessedKey(eventId);
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            log.debug("Event {} already processed by {}", eventId, serviceName);
            return true;
        }
        return false;
    }

    public void markAsProcessed(String eventId) {
        String key = buildProcessedKey(eventId);
        redisTemplate.opsForValue().set(key, "1", EVENT_TTL);
        log.debug("Event {} marked as processed by {}", eventId, serviceName);
    }

    public boolean acquireProcessingLock(String eventId) {
        String lockKey = buildLockKey(eventId);
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", PROCESSING_LOCK_TTL);

        if (Boolean.TRUE.equals(acquired)) {
            log.debug("Acquired processing lock for event {}", eventId);
            return true;
        }

        log.warn("Could not acquire lock for event {} - another instance may be processing", eventId);
        return false;
    }

    public void releaseProcessingLock(String eventId) {
        String lockKey = buildLockKey(eventId);
        redisTemplate.delete(lockKey);
        log.debug("Released processing lock for event {}", eventId);
    }

    public boolean processOnce(String eventId, Runnable processor) {
        if (isAlreadyProcessed(eventId)) {
            log.info("Skipping already processed event: {}", eventId);
            return false;
        }

        if (!acquireProcessingLock(eventId)) {
            log.warn("Could not acquire lock for event: {}", eventId);
            return false;
        }

        try {
            if (isAlreadyProcessed(eventId)) {
                log.info("Event {} was processed by another instance", eventId);
                return false;
            }

            processor.run();

            markAsProcessed(eventId);

            log.info("Successfully processed event: {}", eventId);
            return true;

        } catch (Exception e) {
            log.error("Failed to process event: {}", eventId, e);
            throw e;
        } finally {
            releaseProcessingLock(eventId);
        }
    }

    private String buildProcessedKey(String eventId) {
        return String.format("event:processed:%s:%s", serviceName, eventId);
    }

    private String buildLockKey(String eventId) {
        return String.format("event:lock:%s:%s", serviceName, eventId);
    }
}

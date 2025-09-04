package com.example.shared_common.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
public class ApiIdempotencyService {
    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper;

    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    public ApiIdempotencyService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> Optional<IdempotentResponse<T>> checkAndRetrieve(
            String key,
            String prefix,
            Class<T> responseType
    ) {
        String cacheKey = prefix + ":" + key;
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);

        if (cachedJson != null) {
            try {
                IdempotentResponse<T> cached = objectMapper.readValue(
                        cachedJson,
                        objectMapper.getTypeFactory().constructParametricType(
                                IdempotentResponse.class,
                                responseType
                        )
                );

                return Optional.of(cached);
            } catch (Exception e) {
                log.error("Failed to deserialize cached response for key: {}", cacheKey, e);
                redisTemplate.delete(cacheKey);

                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    public <T> boolean storeResponse(
            String key,
            String prefix,
            T response,
            HttpStatus status
    ) {
        String cacheKey = prefix + ":" + key;

        try {
            IdempotentResponse<T> wrapper = new IdempotentResponse<>(
                    response,
                    status.value(),
                    Instant.now()
            );

            String json = objectMapper.writeValueAsString(wrapper);

            Boolean success = redisTemplate.opsForValue().setIfAbsent(cacheKey, json, DEFAULT_TTL);

            if (Boolean.TRUE.equals(success)) {
                log.debug("Stored idempotent response for key: {}", cacheKey);

                return true;
            } else {
                log.warn("Attempted to store duplicate response for key: {}", cacheKey);

                return false;
            }
        } catch (Exception e) {
            log.error("Failed to serialize response for key: {}", cacheKey, e);

            return false;
        }
    }

    public boolean acquireLock(String idempotencyKey, String prefix, Duration lockDuration) {
        String lockKey = prefix + "lock:" + idempotencyKey;
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", lockDuration);

        return Boolean.TRUE.equals(acquired);
    }

    public void releaseLock(String idempotencyKey, String prefix) {
        String lockKey = prefix + ":lock:" + idempotencyKey;

        redisTemplate.delete(lockKey);
    }

    @Data
    public static class IdempotentResponse<T> {
        private T data;
        private int statusCode;
        private Instant timestamp;

        public IdempotentResponse() {
        }

        public IdempotentResponse(T data, int statusCode, Instant timestamp) {
            this.data = data;
            this.statusCode = statusCode;
            this.timestamp = timestamp;
        }
    }
}

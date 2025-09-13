package com.example.order_service.consumer;

import com.example.events.payment.PaymentFailedEvent;
import com.example.events.payment.PaymentFailureReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentFailedConsumer Unit Tests")
class PaymentFailedConsumerTest {

    @InjectMocks
    private PaymentFailedConsumer paymentFailedConsumer;

    private PaymentFailedEvent validPaymentFailedEvent;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();

        // Create test PaymentFailedEvent with minimum required fields
        validPaymentFailedEvent = new PaymentFailedEvent();
        validPaymentFailedEvent.setOrderId(orderId.toString());
        validPaymentFailedEvent.setFailureReason(PaymentFailureReason.INSUFFICIENT_FUNDS);
        validPaymentFailedEvent.setAttemptNumber(1);
        validPaymentFailedEvent.setMaxAttempts(3);
        validPaymentFailedEvent.setCanRetry(true);
    }

    @Test
    @DisplayName("Should process payment failed event successfully")
    void shouldProcessPaymentFailedEventSuccessfully() {
        // Act & Assert - Should not throw exception
        assertThatCode(() -> paymentFailedConsumer.paymentFailedConsumer(validPaymentFailedEvent))
                .doesNotThrowAnyException();
        
        // Since this consumer only logs, we mainly verify no exceptions are thrown
    }

    @Test
    @DisplayName("Should handle null order ID gracefully")
    void shouldHandleNullOrderIdGracefully() {
        // Arrange
        PaymentFailedEvent eventWithNullId = new PaymentFailedEvent();
        eventWithNullId.setOrderId(null);
        eventWithNullId.setFailureReason(PaymentFailureReason.CARD_DECLINED);
        eventWithNullId.setAttemptNumber(2);
        eventWithNullId.setMaxAttempts(3);
        eventWithNullId.setCanRetry(true);

        // Act & Assert - Should not throw exception due to try-catch
        assertThatCode(() -> paymentFailedConsumer.paymentFailedConsumer(eventWithNullId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle empty order ID gracefully")
    void shouldHandleEmptyOrderIdGracefully() {
        // Arrange
        PaymentFailedEvent eventWithEmptyId = new PaymentFailedEvent();
        eventWithEmptyId.setOrderId("");
        eventWithEmptyId.setFailureReason(PaymentFailureReason.EXPIRED_CARD);
        eventWithEmptyId.setAttemptNumber(1);
        eventWithEmptyId.setMaxAttempts(3);
        eventWithEmptyId.setCanRetry(false);

        // Act & Assert - Should not throw exception due to try-catch
        assertThatCode(() -> paymentFailedConsumer.paymentFailedConsumer(eventWithEmptyId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle null event gracefully")
    void shouldHandleNullEventGracefully() {
        // Act & Assert - Should not throw exception due to try-catch
        assertThatCode(() -> paymentFailedConsumer.paymentFailedConsumer(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle all payment failure reasons")
    void shouldHandleAllPaymentFailureReasons() {
        // Arrange
        PaymentFailureReason[] allReasons = PaymentFailureReason.values();

        // Act & Assert - Should handle all different failure reasons
        for (PaymentFailureReason reason : allReasons) {
            PaymentFailedEvent event = new PaymentFailedEvent();
            event.setOrderId(UUID.randomUUID().toString());
            event.setFailureReason(reason);
            event.setAttemptNumber(1);
            event.setMaxAttempts(3);
            event.setCanRetry(true);
            
            assertThatCode(() -> paymentFailedConsumer.paymentFailedConsumer(event))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("Should handle multiple consecutive events")
    void shouldHandleMultipleConsecutiveEvents() {
        // Arrange
        PaymentFailedEvent event1 = new PaymentFailedEvent();
        event1.setOrderId(UUID.randomUUID().toString());
        event1.setFailureReason(PaymentFailureReason.INSUFFICIENT_FUNDS);
        event1.setAttemptNumber(1);
        event1.setMaxAttempts(3);
        event1.setCanRetry(true);

        PaymentFailedEvent event2 = new PaymentFailedEvent();
        event2.setOrderId(UUID.randomUUID().toString());
        event2.setFailureReason(PaymentFailureReason.CARD_DECLINED);
        event2.setAttemptNumber(2);
        event2.setMaxAttempts(3);
        event2.setCanRetry(true);

        PaymentFailedEvent event3 = new PaymentFailedEvent();
        event3.setOrderId(UUID.randomUUID().toString());
        event3.setFailureReason(PaymentFailureReason.FRAUD_DETECTED);
        event3.setAttemptNumber(3);
        event3.setMaxAttempts(3);
        event3.setCanRetry(false);

        // Act & Assert - Should handle all events without throwing exceptions
        assertThatCode(() -> {
            paymentFailedConsumer.paymentFailedConsumer(event1);
            paymentFailedConsumer.paymentFailedConsumer(event2);
            paymentFailedConsumer.paymentFailedConsumer(event3);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle event with different order ID formats")
    void shouldHandleEventWithDifferentOrderIdFormats() {
        // Arrange
        String[] orderIdFormats = {
            UUID.randomUUID().toString(), // Valid UUID
            "ORD-12345", // Order number format
            "invalid-uuid-format", // Invalid UUID
            "12345", // Numeric string
            "", // Empty string
            "   ", // Whitespace
            null // Null
        };

        // Act & Assert - Should handle all different order ID formats
        for (String orderIdFormat : orderIdFormats) {
            PaymentFailedEvent event = new PaymentFailedEvent();
            event.setOrderId(orderIdFormat);
            event.setFailureReason(PaymentFailureReason.PROCESSING_ERROR);
            event.setAttemptNumber(1);
            event.setMaxAttempts(1);
            event.setCanRetry(false);
            
            assertThatCode(() -> paymentFailedConsumer.paymentFailedConsumer(event))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("Should handle retry scenarios")
    void shouldHandleRetryScenarios() {
        // Arrange
        PaymentFailedEvent retryableEvent = new PaymentFailedEvent();
        retryableEvent.setOrderId(orderId.toString());
        retryableEvent.setFailureReason(PaymentFailureReason.INSUFFICIENT_FUNDS);
        retryableEvent.setAttemptNumber(1);
        retryableEvent.setMaxAttempts(3);
        retryableEvent.setCanRetry(true);

        PaymentFailedEvent nonRetryableEvent = new PaymentFailedEvent();
        nonRetryableEvent.setOrderId(orderId.toString());
        nonRetryableEvent.setFailureReason(PaymentFailureReason.FRAUD_DETECTED);
        nonRetryableEvent.setAttemptNumber(1);
        nonRetryableEvent.setMaxAttempts(1);
        nonRetryableEvent.setCanRetry(false);

        PaymentFailedEvent maxAttemptsReachedEvent = new PaymentFailedEvent();
        maxAttemptsReachedEvent.setOrderId(orderId.toString());
        maxAttemptsReachedEvent.setFailureReason(PaymentFailureReason.MAXIMUM_NUMBER_OF_RETRIES_REACHED);
        maxAttemptsReachedEvent.setAttemptNumber(3);
        maxAttemptsReachedEvent.setMaxAttempts(3);
        maxAttemptsReachedEvent.setCanRetry(false);

        // Act & Assert - Should handle all retry scenarios
        assertThatCode(() -> {
            paymentFailedConsumer.paymentFailedConsumer(retryableEvent);
            paymentFailedConsumer.paymentFailedConsumer(nonRetryableEvent);
            paymentFailedConsumer.paymentFailedConsumer(maxAttemptsReachedEvent);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should be idempotent - processing same event multiple times")
    void shouldBeIdempotentProcessingSameEventMultipleTimes() {
        // Act & Assert - Should handle same event multiple times without issues
        assertThatCode(() -> {
            paymentFailedConsumer.paymentFailedConsumer(validPaymentFailedEvent);
            paymentFailedConsumer.paymentFailedConsumer(validPaymentFailedEvent);
            paymentFailedConsumer.paymentFailedConsumer(validPaymentFailedEvent);
        }).doesNotThrowAnyException();
        
        // Since this consumer only logs, processing the same event multiple times should be safe
    }

    @Test
    @DisplayName("Should handle high volume of events")
    void shouldHandleHighVolumeOfEvents() {
        // Arrange
        int eventCount = 1000;

        // Act & Assert - Should handle high volume without issues
        assertThatCode(() -> {
            for (int i = 0; i < eventCount; i++) {
                PaymentFailedEvent event = new PaymentFailedEvent();
                event.setOrderId(UUID.randomUUID().toString());
                event.setFailureReason(PaymentFailureReason.values()[i % PaymentFailureReason.values().length]);
                event.setAttemptNumber(1);
                event.setMaxAttempts(3);
                event.setCanRetry(i % 2 == 0);
                paymentFailedConsumer.paymentFailedConsumer(event);
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle all combinations of attempt numbers and max attempts")
    void shouldHandleAllCombinationsOfAttemptNumbersAndMaxAttempts() {
        // Arrange
        int[] attemptNumbers = {1, 2, 3, 5};
        int[] maxAttempts = {1, 3, 5};
        boolean[] canRetryValues = {true, false};

        // Act & Assert - Should handle all combinations
        for (int attemptNumber : attemptNumbers) {
            for (int maxAttempt : maxAttempts) {
                for (boolean canRetry : canRetryValues) {
                    PaymentFailedEvent event = new PaymentFailedEvent();
                    event.setOrderId(UUID.randomUUID().toString());
                    event.setFailureReason(PaymentFailureReason.PROCESSING_ERROR);
                    event.setAttemptNumber(attemptNumber);
                    event.setMaxAttempts(maxAttempt);
                    event.setCanRetry(canRetry);
                    
                    assertThatCode(() -> paymentFailedConsumer.paymentFailedConsumer(event))
                            .doesNotThrowAnyException();
                }
            }
        }
    }

    @Test
    @DisplayName("Should maintain thread safety")
    void shouldMaintainThreadSafety() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        int eventsPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // Act - Create multiple threads processing events concurrently
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < eventsPerThread; j++) {
                    PaymentFailedEvent event = new PaymentFailedEvent();
                    event.setOrderId(UUID.randomUUID().toString());
                    event.setFailureReason(PaymentFailureReason.values()[j % PaymentFailureReason.values().length]);
                    event.setAttemptNumber(j + 1);
                    event.setMaxAttempts(3);
                    event.setCanRetry(j < eventsPerThread / 2);
                    
                    assertThatCode(() -> paymentFailedConsumer.paymentFailedConsumer(event))
                            .doesNotThrowAnyException();
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert - All threads should complete without exceptions
        // If we reach this point, thread safety test passed
    }
}
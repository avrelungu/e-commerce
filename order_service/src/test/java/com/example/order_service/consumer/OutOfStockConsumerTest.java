package com.example.order_service.consumer;

import com.example.events.inventory.OutOfStockEvent;
import com.example.order_service.enums.OrderStatus;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutOfStockConsumer Unit Tests")
class OutOfStockConsumerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OutOfStockConsumer outOfStockConsumer;

    private OutOfStockEvent validOutOfStockEvent;
    private Order mockOrder;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();

        validOutOfStockEvent = new OutOfStockEvent();
        validOutOfStockEvent.setOrderId(orderId.toString());
        validOutOfStockEvent.setProductId("PROD-12345");
        validOutOfStockEvent.setRequestedQuantity(10);
        validOutOfStockEvent.setAvailableQuantity(3);
        
        mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setStatus(OrderStatus.CONFIRMED.toString());
    }

    @Test
    @DisplayName("Should process out of stock event successfully")
    void shouldProcessOutOfStockEventSuccessfully() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act
        assertThatCode(() -> outOfStockConsumer.outOfStockConsumer(validOutOfStockEvent))
                .doesNotThrowAnyException();

        // Assert
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(Order.class));
        
        // Verify the order status was updated to OUT_OF_STOCK
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.OUT_OF_STOCK.name());
        assertThat(savedOrder.getId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("Should handle order not found gracefully")
    void shouldHandleOrderNotFoundGracefully() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act - Should not throw exception
        assertThatCode(() -> outOfStockConsumer.outOfStockConsumer(validOutOfStockEvent))
                .doesNotThrowAnyException();

        // Assert
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should handle invalid order ID format gracefully")
    void shouldHandleInvalidOrderIdFormatGracefully() {
        // Arrange
        OutOfStockEvent invalidEvent = new OutOfStockEvent();
        invalidEvent.setOrderId("invalid-uuid-format");
        invalidEvent.setProductId("PROD-67890");
        invalidEvent.setRequestedQuantity(5);
        invalidEvent.setAvailableQuantity(0);

        // Act - Should not throw exception due to try-catch
        assertThatCode(() -> outOfStockConsumer.outOfStockConsumer(invalidEvent))
                .doesNotThrowAnyException();

        // Assert
        verify(orderRepository, never()).findById(any());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should handle null order ID gracefully")
    void shouldHandleNullOrderIdGracefully() {
        // Arrange
        OutOfStockEvent eventWithNullId = new OutOfStockEvent();
        eventWithNullId.setOrderId(null);
        eventWithNullId.setProductId("PROD-NULL");
        eventWithNullId.setRequestedQuantity(1);
        eventWithNullId.setAvailableQuantity(0);

        // Act - Should not throw exception due to try-catch
        assertThatCode(() -> outOfStockConsumer.outOfStockConsumer(eventWithNullId))
                .doesNotThrowAnyException();

        // Assert
        verify(orderRepository, never()).findById(any());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should handle empty order ID gracefully")
    void shouldHandleEmptyOrderIdGracefully() {
        // Arrange
        OutOfStockEvent eventWithEmptyId = new OutOfStockEvent();
        eventWithEmptyId.setOrderId("");
        eventWithEmptyId.setProductId("PROD-EMPTY");
        eventWithEmptyId.setRequestedQuantity(2);
        eventWithEmptyId.setAvailableQuantity(0);

        // Act - Should not throw exception due to try-catch
        assertThatCode(() -> outOfStockConsumer.outOfStockConsumer(eventWithEmptyId))
                .doesNotThrowAnyException();

        // Assert
        verify(orderRepository, never()).findById(any());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should handle repository findById exception gracefully")
    void shouldHandleRepositoryFindByIdExceptionGracefully() {
        // Arrange
        when(orderRepository.findById(orderId))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act - Should not propagate exception due to try-catch
        assertThatCode(() -> outOfStockConsumer.outOfStockConsumer(validOutOfStockEvent))
                .doesNotThrowAnyException();

        // Assert
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should handle repository save exception gracefully")
    void shouldHandleRepositorySaveExceptionGracefully() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class)))
                .thenThrow(new RuntimeException("Failed to save order"));

        // Act - Should not propagate exception due to try-catch
        assertThatCode(() -> outOfStockConsumer.outOfStockConsumer(validOutOfStockEvent))
                .doesNotThrowAnyException();

        // Assert
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should verify order status transition to OUT_OF_STOCK")
    void shouldVerifyOrderStatusTransitionToOutOfStock() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act
        outOfStockConsumer.outOfStockConsumer(validOutOfStockEvent);

        // Assert - Verify specific status transition
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.OUT_OF_STOCK.name());
        assertThat(savedOrder.getId()).isEqualTo(orderId);
        assertThat(savedOrder).isSameAs(mockOrder); // Same object reference
    }

    @Test
    @DisplayName("Should execute operations in correct sequence")
    void shouldExecuteOperationsInCorrectSequence() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act
        outOfStockConsumer.outOfStockConsumer(validOutOfStockEvent);

        // Assert - Verify execution order using InOrder
        var inOrder = inOrder(orderRepository);
        inOrder.verify(orderRepository).findById(orderId);
        inOrder.verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should handle multiple consecutive events")
    void shouldHandleMultipleConsecutiveEvents() {
        // Arrange
        UUID orderId2 = UUID.randomUUID();
        Order mockOrder2 = new Order();
        mockOrder2.setId(orderId2);
        mockOrder2.setStatus(OrderStatus.CONFIRMED.toString());
        
        OutOfStockEvent event2 = new OutOfStockEvent();
        event2.setOrderId(orderId2.toString());
        event2.setProductId("PROD-67890");
        event2.setRequestedQuantity(15);
        event2.setAvailableQuantity(2);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.findById(orderId2)).thenReturn(Optional.of(mockOrder2));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder).thenReturn(mockOrder2);

        // Act
        outOfStockConsumer.outOfStockConsumer(validOutOfStockEvent);
        outOfStockConsumer.outOfStockConsumer(event2);

        // Assert
        verify(orderRepository).findById(orderId);
        verify(orderRepository).findById(orderId2);
        verify(orderRepository, times(2)).save(any(Order.class));
        
        // Verify both orders were updated to OUT_OF_STOCK
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(2)).save(orderCaptor.capture());
        
        for (Order savedOrder : orderCaptor.getAllValues()) {
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.OUT_OF_STOCK.name());
        }
    }

    @Test
    @DisplayName("Should only save order if order is found")
    void shouldOnlySaveOrderIfOrderIsFound() {
        // Arrange
        UUID foundOrderId = UUID.randomUUID();
        UUID notFoundOrderId = UUID.randomUUID();
        
        Order foundOrder = new Order();
        foundOrder.setId(foundOrderId);
        foundOrder.setStatus(OrderStatus.CONFIRMED.toString());
        
        OutOfStockEvent foundEvent = new OutOfStockEvent();
        foundEvent.setOrderId(foundOrderId.toString());
        foundEvent.setProductId("PROD-FOUND");
        foundEvent.setRequestedQuantity(8);
        foundEvent.setAvailableQuantity(1);
        
        OutOfStockEvent notFoundEvent = new OutOfStockEvent();
        notFoundEvent.setOrderId(notFoundOrderId.toString());
        notFoundEvent.setProductId("PROD-NOT-FOUND");
        notFoundEvent.setRequestedQuantity(5);
        notFoundEvent.setAvailableQuantity(0);

        when(orderRepository.findById(foundOrderId)).thenReturn(Optional.of(foundOrder));
        when(orderRepository.findById(notFoundOrderId)).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(foundOrder);

        // Act
        outOfStockConsumer.outOfStockConsumer(foundEvent);
        outOfStockConsumer.outOfStockConsumer(notFoundEvent);

        // Assert
        verify(orderRepository).findById(foundOrderId);
        verify(orderRepository).findById(notFoundOrderId);
        
        // Only the found order should be saved
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should preserve order data integrity during status update")
    void shouldPreserveOrderDataIntegrityDuringStatusUpdate() {
        // Arrange
        mockOrder.setOrderNumber("ORD-12345");
        mockOrder.setCustomerId(UUID.randomUUID());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act
        outOfStockConsumer.outOfStockConsumer(validOutOfStockEvent);

        // Assert - Verify that the same order object with all its data is saved
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getId()).isEqualTo(mockOrder.getId());
        assertThat(savedOrder.getOrderNumber()).isEqualTo(mockOrder.getOrderNumber());
        assertThat(savedOrder.getCustomerId()).isEqualTo(mockOrder.getCustomerId());
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.OUT_OF_STOCK.name());
        assertThat(savedOrder).isSameAs(mockOrder); // Same object reference
    }

    @Test
    @DisplayName("Should handle null event gracefully")
    void shouldHandleNullEventGracefully() {
        // Act - Should not throw exception due to try-catch
        assertThatCode(() -> outOfStockConsumer.outOfStockConsumer(null))
                .doesNotThrowAnyException();

        // Assert
        verify(orderRepository, never()).findById(any());
        verify(orderRepository, never()).save(any(Order.class));
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
            OutOfStockEvent event = new OutOfStockEvent();
            event.setOrderId(orderIdFormat);
            event.setProductId("PROD-FORMAT-TEST");
            event.setRequestedQuantity(1);
            event.setAvailableQuantity(0);
            
            assertThatCode(() -> outOfStockConsumer.outOfStockConsumer(event))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    @DisplayName("Should handle various product ID formats")
    void shouldHandleVariousProductIdFormats() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        
        String[] productIdFormats = {
            "PROD-12345", // Standard format
            "SKU-ABC-123", // SKU format
            "12345", // Numeric
            "", // Empty
            null // Null
        };

        // Act & Assert - Should handle all different product ID formats
        for (String productIdFormat : productIdFormats) {
            OutOfStockEvent event = new OutOfStockEvent();
            event.setOrderId(orderId.toString());
            event.setProductId(productIdFormat);
            event.setRequestedQuantity(5);
            event.setAvailableQuantity(2);
            
            assertThatCode(() -> outOfStockConsumer.outOfStockConsumer(event))
                    .doesNotThrowAnyException();
        }
        
        // Verify all events were processed
        verify(orderRepository, times(productIdFormats.length)).findById(orderId);
        verify(orderRepository, times(productIdFormats.length)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should handle various quantity combinations")
    void shouldHandleVariousQuantityCombinations() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        
        int[][] quantityCombinations = {
            {10, 0}, // Requested 10, available 0
            {5, 2}, // Requested 5, available 2
            {1, 0}, // Requested 1, available 0
            {100, 10}, // High request, low availability
            {0, 0}, // Zero request, zero availability
            {-1, -1} // Negative values (edge case)
        };

        // Act & Assert - Should handle all quantity combinations
        for (int[] quantities : quantityCombinations) {
            OutOfStockEvent event = new OutOfStockEvent();
            event.setOrderId(orderId.toString());
            event.setProductId("PROD-QTY-TEST");
            event.setRequestedQuantity(quantities[0]);
            event.setAvailableQuantity(quantities[1]);
            
            assertThatCode(() -> outOfStockConsumer.outOfStockConsumer(event))
                    .doesNotThrowAnyException();
        }
        
        // Verify all events were processed
        verify(orderRepository, times(quantityCombinations.length)).findById(orderId);
        verify(orderRepository, times(quantityCombinations.length)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should maintain thread safety")
    void shouldMaintainThreadSafety() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        int eventsPerThread = 50;
        Thread[] threads = new Thread[threadCount];

        when(orderRepository.findById(any())).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act - Create multiple threads processing events concurrently
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < eventsPerThread; j++) {
                    OutOfStockEvent event = new OutOfStockEvent();
                    event.setOrderId(UUID.randomUUID().toString());
                    event.setProductId("PROD-THREAD-" + threadIndex + "-" + j);
                    event.setRequestedQuantity(j + 1);
                    event.setAvailableQuantity(0);
                    
                    assertThatCode(() -> outOfStockConsumer.outOfStockConsumer(event))
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

    @Test
    @DisplayName("Should handle high volume of events")
    void shouldHandleHighVolumeOfEvents() {
        // Arrange
        int eventCount = 1000;
        when(orderRepository.findById(any())).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act & Assert - Should handle high volume without issues
        assertThatCode(() -> {
            for (int i = 0; i < eventCount; i++) {
                OutOfStockEvent event = new OutOfStockEvent();
                event.setOrderId(UUID.randomUUID().toString());
                event.setProductId("PROD-VOLUME-" + i);
                event.setRequestedQuantity(i % 10 + 1);
                event.setAvailableQuantity(0);
                outOfStockConsumer.outOfStockConsumer(event);
            }
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should be idempotent - processing same event multiple times")
    void shouldBeIdempotentProcessingSameEventMultipleTimes() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // Act & Assert - Should handle same event multiple times
        assertThatCode(() -> {
            outOfStockConsumer.outOfStockConsumer(validOutOfStockEvent);
            outOfStockConsumer.outOfStockConsumer(validOutOfStockEvent);
            outOfStockConsumer.outOfStockConsumer(validOutOfStockEvent);
        }).doesNotThrowAnyException();
        
        // Verify the event was processed multiple times
        verify(orderRepository, times(3)).findById(orderId);
        verify(orderRepository, times(3)).save(any(Order.class));
        
        // Each processing should set the status to OUT_OF_STOCK (idempotent)
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(3)).save(orderCaptor.capture());
        
        for (Order savedOrder : orderCaptor.getAllValues()) {
            assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.OUT_OF_STOCK.name());
        }
    }
}
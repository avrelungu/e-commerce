package com.example.order_service.consumer;

import com.example.events.payment.PaymentProcessedEvent;
import com.example.order_service.enums.OrderStatus;
import com.example.order_service.model.Order;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.OrderStateService;
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
@DisplayName("PaymentProcessedConsumer Unit Tests")
class PaymentProcessedConsumerTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderStateService orderStateService;

    @InjectMocks
    private PaymentProcessedConsumer paymentProcessedConsumer;

    private PaymentProcessedEvent validPaymentProcessedEvent;
    private Order mockOrder;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();

        // Create test PaymentProcessedEvent
        validPaymentProcessedEvent = new PaymentProcessedEvent();
        validPaymentProcessedEvent.setOrderId(orderId.toString());

        // Create mock Order
        mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setStatus(OrderStatus.CONFIRMED.toString());
    }

    @Test
    @DisplayName("Should process payment processed event successfully")
    void shouldProcessPaymentProcessedEventSuccessfully() throws Exception {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        doNothing().when(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.PAID));

        // Act
        paymentProcessedConsumer.paymentProcessedConsumer(validPaymentProcessedEvent);

        // Assert
        verify(orderRepository).findById(orderId);
        verify(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.PAID));
        
        // Verify the correct order object is passed
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        ArgumentCaptor<OrderStatus> statusCaptor = ArgumentCaptor.forClass(OrderStatus.class);
        verify(orderStateService).updateOrderStatus(orderCaptor.capture(), statusCaptor.capture());
        
        assertThat(orderCaptor.getValue().getId()).isEqualTo(orderId);
        assertThat(statusCaptor.getValue()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("Should handle order not found gracefully")
    void shouldHandleOrderNotFoundGracefully() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act - Should not throw exception
        assertThatCode(() -> paymentProcessedConsumer.paymentProcessedConsumer(validPaymentProcessedEvent))
                .doesNotThrowAnyException();

        // Assert
        verify(orderRepository).findById(orderId);
        verify(orderStateService, never()).updateOrderStatus(any(Order.class), any(OrderStatus.class));
    }

    @Test
    @DisplayName("Should handle invalid order ID format gracefully")
    void shouldHandleInvalidOrderIdFormatGracefully() {
        // Arrange
        PaymentProcessedEvent invalidEvent = new PaymentProcessedEvent();
        invalidEvent.setOrderId("invalid-uuid-format");

        // Act - Should not throw exception due to try-catch
        assertThatCode(() -> paymentProcessedConsumer.paymentProcessedConsumer(invalidEvent))
                .doesNotThrowAnyException();

        // Assert
        verify(orderRepository, never()).findById(any());
        verify(orderStateService, never()).updateOrderStatus(any(Order.class), any(OrderStatus.class));
    }

    @Test
    @DisplayName("Should handle null order ID gracefully")
    void shouldHandleNullOrderIdGracefully() {
        // Arrange
        PaymentProcessedEvent eventWithNullId = new PaymentProcessedEvent();
        eventWithNullId.setOrderId(null);

        // Act - Should not throw exception due to try-catch
        assertThatCode(() -> paymentProcessedConsumer.paymentProcessedConsumer(eventWithNullId))
                .doesNotThrowAnyException();

        // Assert
        verify(orderRepository, never()).findById(any());
        verify(orderStateService, never()).updateOrderStatus(any(Order.class), any(OrderStatus.class));
    }

    @Test
    @DisplayName("Should handle empty order ID gracefully")
    void shouldHandleEmptyOrderIdGracefully() {
        // Arrange
        PaymentProcessedEvent eventWithEmptyId = new PaymentProcessedEvent();
        eventWithEmptyId.setOrderId("");

        // Act - Should not throw exception due to try-catch
        assertThatCode(() -> paymentProcessedConsumer.paymentProcessedConsumer(eventWithEmptyId))
                .doesNotThrowAnyException();

        // Assert
        verify(orderRepository, never()).findById(any());
        verify(orderStateService, never()).updateOrderStatus(any(Order.class), any(OrderStatus.class));
    }

    @Test
    @DisplayName("Should handle repository exception gracefully")
    void shouldHandleRepositoryExceptionGracefully() {
        // Arrange
        when(orderRepository.findById(orderId))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act - Should not propagate exception due to try-catch
        assertThatCode(() -> paymentProcessedConsumer.paymentProcessedConsumer(validPaymentProcessedEvent))
                .doesNotThrowAnyException();

        // Assert
        verify(orderRepository).findById(orderId);
        verify(orderStateService, never()).updateOrderStatus(any(Order.class), any(OrderStatus.class));
    }

    @Test
    @DisplayName("Should handle order state service exception gracefully")
    void shouldHandleOrderStateServiceExceptionGracefully() throws Exception {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        doThrow(new RuntimeException("State transition not allowed"))
                .when(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.PAID));

        // Act - Should not propagate exception due to try-catch
        assertThatCode(() -> paymentProcessedConsumer.paymentProcessedConsumer(validPaymentProcessedEvent))
                .doesNotThrowAnyException();

        // Assert
        verify(orderRepository).findById(orderId);
        verify(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.PAID));
    }

    @Test
    @DisplayName("Should verify order status transition from CONFIRMED to PAID")
    void shouldVerifyOrderStatusTransitionFromConfirmedToPaid() throws Exception {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        // Act
        paymentProcessedConsumer.paymentProcessedConsumer(validPaymentProcessedEvent);

        // Assert - Verify specific status transition
        verify(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.PAID));
        
        // Verify the correct order object is passed
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderStateService).updateOrderStatus(orderCaptor.capture(), eq(OrderStatus.PAID));
        assertThat(orderCaptor.getValue().getId()).isEqualTo(orderId);
        assertThat(orderCaptor.getValue()).isSameAs(mockOrder);
    }

    @Test
    @DisplayName("Should execute operations in correct sequence")
    void shouldExecuteOperationsInCorrectSequence() throws Exception {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        // Act
        paymentProcessedConsumer.paymentProcessedConsumer(validPaymentProcessedEvent);

        // Assert - Verify execution order using InOrder
        var inOrder = inOrder(orderRepository, orderStateService);
        inOrder.verify(orderRepository).findById(orderId);
        inOrder.verify(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.PAID));
    }

    @Test
    @DisplayName("Should handle multiple consecutive events")
    void shouldHandleMultipleConsecutiveEvents() throws Exception {
        // Arrange
        UUID orderId2 = UUID.randomUUID();
        Order mockOrder2 = new Order();
        mockOrder2.setId(orderId2);
        
        PaymentProcessedEvent event2 = new PaymentProcessedEvent();
        event2.setOrderId(orderId2.toString());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.findById(orderId2)).thenReturn(Optional.of(mockOrder2));

        // Act
        paymentProcessedConsumer.paymentProcessedConsumer(validPaymentProcessedEvent);
        paymentProcessedConsumer.paymentProcessedConsumer(event2);

        // Assert
        verify(orderRepository).findById(orderId);
        verify(orderRepository).findById(orderId2);
        verify(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.PAID));
        verify(orderStateService).updateOrderStatus(eq(mockOrder2), eq(OrderStatus.PAID));
        
        // Verify both calls were made
        verify(orderStateService, times(2)).updateOrderStatus(any(Order.class), eq(OrderStatus.PAID));
    }

    @Test
    @DisplayName("Should only update status if order is found")
    void shouldOnlyUpdateStatusIfOrderIsFound() throws Exception {
        // Arrange
        UUID foundOrderId = UUID.randomUUID();
        UUID notFoundOrderId = UUID.randomUUID();
        
        Order foundOrder = new Order();
        foundOrder.setId(foundOrderId);
        
        PaymentProcessedEvent foundEvent = new PaymentProcessedEvent();
        foundEvent.setOrderId(foundOrderId.toString());
        
        PaymentProcessedEvent notFoundEvent = new PaymentProcessedEvent();
        notFoundEvent.setOrderId(notFoundOrderId.toString());

        when(orderRepository.findById(foundOrderId)).thenReturn(Optional.of(foundOrder));
        when(orderRepository.findById(notFoundOrderId)).thenReturn(Optional.empty());

        // Act
        paymentProcessedConsumer.paymentProcessedConsumer(foundEvent);
        paymentProcessedConsumer.paymentProcessedConsumer(notFoundEvent);

        // Assert
        verify(orderRepository).findById(foundOrderId);
        verify(orderRepository).findById(notFoundOrderId);
        
        // Only the found order should have status updated
        verify(orderStateService, times(1)).updateOrderStatus(eq(foundOrder), eq(OrderStatus.PAID));
        verify(orderStateService, never()).updateOrderStatus(any(Order.class), eq(OrderStatus.CONFIRMED));
        verify(orderStateService, never()).updateOrderStatus(any(Order.class), eq(OrderStatus.PENDING));
    }

    @Test
    @DisplayName("Should preserve order data integrity during status update")
    void shouldPreserveOrderDataIntegrityDuringStatusUpdate() throws Exception {
        // Arrange
        mockOrder.setOrderNumber("ORD-12345");
        mockOrder.setCustomerId(UUID.randomUUID());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        // Act
        paymentProcessedConsumer.paymentProcessedConsumer(validPaymentProcessedEvent);

        // Assert - Verify that the same order object with all its data is passed to state service
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderStateService).updateOrderStatus(orderCaptor.capture(), eq(OrderStatus.PAID));
        
        Order capturedOrder = orderCaptor.getValue();
        assertThat(capturedOrder.getId()).isEqualTo(mockOrder.getId());
        assertThat(capturedOrder.getOrderNumber()).isEqualTo(mockOrder.getOrderNumber());
        assertThat(capturedOrder.getCustomerId()).isEqualTo(mockOrder.getCustomerId());
        assertThat(capturedOrder).isSameAs(mockOrder); // Same object reference
    }
}
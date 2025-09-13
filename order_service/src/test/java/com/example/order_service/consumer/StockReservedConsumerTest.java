package com.example.order_service.consumer;

import com.example.events.inventory.StockReservedEvent;
import com.example.events.payment.PaymentRequestEvent;
import com.example.order_service.enums.OrderStatus;
import com.example.order_service.event.PaymentRequest;
import com.example.order_service.exceptions.AppException;
import com.example.order_service.mapper.PaymentRequestMapper;
import com.example.order_service.model.Order;
import com.example.order_service.publisher.EventPublisher;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockReservedConsumer Unit Tests")
class StockReservedConsumerTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @Mock
    private PaymentRequestMapper paymentRequestMapper;
    
    @Mock
    private OrderStateService orderStateService;

    @InjectMocks
    private StockReservedConsumer stockReservedConsumer;

    private StockReservedEvent validStockReservedEvent;
    private Order mockOrder;
    private PaymentRequestEvent mockPaymentRequestEvent;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        
        // Set up the paymentRequestTopic field using reflection
        ReflectionTestUtils.setField(stockReservedConsumer, "paymentRequestTopic", "payment-request");

        // Create test StockReservedEvent
        validStockReservedEvent = new StockReservedEvent();
        validStockReservedEvent.setOrderId(orderId.toString());

        // Create mock Order
        mockOrder = new Order();
        mockOrder.setId(orderId);
        mockOrder.setStatus(OrderStatus.PENDING.toString());

        // Create mock PaymentRequestEvent
        mockPaymentRequestEvent = new PaymentRequestEvent();
    }

    @Test
    @DisplayName("Should process stock reserved event successfully")
    void shouldProcessStockReservedEventSuccessfully() throws Exception {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(paymentRequestMapper.toPaymentRequestEvent(mockOrder)).thenReturn(mockPaymentRequestEvent);
        doNothing().when(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.CONFIRMED));

        // Act
        stockReservedConsumer.stockReservedConsumer(validStockReservedEvent);

        // Assert
        verify(orderRepository).findById(orderId);
        verify(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.CONFIRMED));
        verify(paymentRequestMapper).toPaymentRequestEvent(mockOrder);
        
        // Verify event publication
        ArgumentCaptor<PaymentRequest> eventCaptor = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        
        PaymentRequest publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getTopic()).isEqualTo("payment-request");
        assertThat(publishedEvent.getPartitionKey()).isEqualTo(orderId.toString());
        assertThat(publishedEvent.getPayload()).isEqualTo(mockPaymentRequestEvent);
    }

    @Test
    @DisplayName("Should throw RuntimeException when order not found")
    void shouldThrowRuntimeExceptionWhenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> stockReservedConsumer.stockReservedConsumer(validStockReservedEvent))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(AppException.class);

        verify(orderRepository).findById(orderId);
        verify(orderStateService, never()).updateOrderStatus(any(Order.class), any(OrderStatus.class));
        verify(paymentRequestMapper, never()).toPaymentRequestEvent(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should handle invalid order ID format")
    void shouldHandleInvalidOrderIdFormat() {
        // Arrange
        StockReservedEvent invalidEvent = new StockReservedEvent();
        invalidEvent.setOrderId("invalid-uuid-format");

        // Act & Assert
        assertThatThrownBy(() -> stockReservedConsumer.stockReservedConsumer(invalidEvent))
                .isInstanceOf(IllegalArgumentException.class);

        verify(orderRepository, never()).findById(any());
        verify(orderStateService, never()).updateOrderStatus(any(Order.class), any(OrderStatus.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should propagate exception when order state update fails")
    void shouldPropagateExceptionWhenOrderStateUpdateFails() throws Exception {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        doThrow(new RuntimeException("State transition not allowed"))
                .when(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.CONFIRMED));

        // Act & Assert
        assertThatThrownBy(() -> stockReservedConsumer.stockReservedConsumer(validStockReservedEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("State transition not allowed");

        verify(orderRepository).findById(orderId);
        verify(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.CONFIRMED));
        verify(paymentRequestMapper, never()).toPaymentRequestEvent(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should propagate exception when payment mapper fails")
    void shouldPropagateExceptionWhenPaymentMapperFails() throws Exception {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        doNothing().when(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.CONFIRMED));
        when(paymentRequestMapper.toPaymentRequestEvent(mockOrder))
                .thenThrow(new RuntimeException("Mapping failed"));

        // Act & Assert
        assertThatThrownBy(() -> stockReservedConsumer.stockReservedConsumer(validStockReservedEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mapping failed");

        verify(orderRepository).findById(orderId);
        verify(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.CONFIRMED));
        verify(paymentRequestMapper).toPaymentRequestEvent(mockOrder);
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should propagate exception when event publishing fails")
    void shouldPropagateExceptionWhenEventPublishingFails() throws Exception {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        doNothing().when(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.CONFIRMED));
        when(paymentRequestMapper.toPaymentRequestEvent(mockOrder)).thenReturn(mockPaymentRequestEvent);
        doThrow(new RuntimeException("Kafka broker unavailable"))
                .when(eventPublisher).publish(any(PaymentRequest.class));

        // Act & Assert
        assertThatThrownBy(() -> stockReservedConsumer.stockReservedConsumer(validStockReservedEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Kafka broker unavailable");

        verify(orderRepository).findById(orderId);
        verify(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.CONFIRMED));
        verify(paymentRequestMapper).toPaymentRequestEvent(mockOrder);
        verify(eventPublisher).publish(any(PaymentRequest.class));
    }

    @Test
    @DisplayName("Should handle null order ID in event")
    void shouldHandleNullOrderIdInEvent() {
        // Arrange
        StockReservedEvent eventWithNullId = new StockReservedEvent();
        eventWithNullId.setOrderId(null);

        // Act & Assert
        assertThatThrownBy(() -> stockReservedConsumer.stockReservedConsumer(eventWithNullId))
                .isInstanceOf(Exception.class);

        verify(orderRepository, never()).findById(any());
        verify(orderStateService, never()).updateOrderStatus(any(Order.class), any(OrderStatus.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should handle empty order ID in event")
    void shouldHandleEmptyOrderIdInEvent() {
        // Arrange
        StockReservedEvent eventWithEmptyId = new StockReservedEvent();
        eventWithEmptyId.setOrderId("");

        // Act & Assert
        assertThatThrownBy(() -> stockReservedConsumer.stockReservedConsumer(eventWithEmptyId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(orderRepository, never()).findById(any());
        verify(orderStateService, never()).updateOrderStatus(any(Order.class), any(OrderStatus.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should verify order status transition from PENDING to CONFIRMED")
    void shouldVerifyOrderStatusTransitionFromPendingToConfirmed() throws Exception {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(paymentRequestMapper.toPaymentRequestEvent(mockOrder)).thenReturn(mockPaymentRequestEvent);

        // Act
        stockReservedConsumer.stockReservedConsumer(validStockReservedEvent);

        // Assert - Verify specific status transition
        verify(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.CONFIRMED));
        
        // Verify the correct order object is passed
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderStateService).updateOrderStatus(orderCaptor.capture(), eq(OrderStatus.CONFIRMED));
        assertThat(orderCaptor.getValue().getId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("Should verify payment request event contains correct order data")
    void shouldVerifyPaymentRequestEventContainsCorrectOrderData() throws Exception {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(paymentRequestMapper.toPaymentRequestEvent(mockOrder)).thenReturn(mockPaymentRequestEvent);

        // Act
        stockReservedConsumer.stockReservedConsumer(validStockReservedEvent);

        // Assert - Verify mapper is called with the correct order
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(paymentRequestMapper).toPaymentRequestEvent(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getId()).isEqualTo(orderId);
        assertThat(orderCaptor.getValue()).isSameAs(mockOrder);
    }

    @Test
    @DisplayName("Should execute operations in correct sequence")
    void shouldExecuteOperationsInCorrectSequence() throws Exception {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(paymentRequestMapper.toPaymentRequestEvent(mockOrder)).thenReturn(mockPaymentRequestEvent);

        // Act
        stockReservedConsumer.stockReservedConsumer(validStockReservedEvent);

        // Assert - Verify execution order using InOrder
        var inOrder = inOrder(orderRepository, orderStateService, paymentRequestMapper, eventPublisher);
        inOrder.verify(orderRepository).findById(orderId);
        inOrder.verify(orderStateService).updateOrderStatus(eq(mockOrder), eq(OrderStatus.CONFIRMED));
        inOrder.verify(paymentRequestMapper).toPaymentRequestEvent(mockOrder);
        inOrder.verify(eventPublisher).publish(any(PaymentRequest.class));
    }
}
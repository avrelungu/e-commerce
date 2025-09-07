package com.example.payment_service.service;

import com.example.events.payment.RefundProcessedEvent;
import com.example.events.payment.RefundFailedEvent;
import com.example.events.payment.RefundType;
import com.example.events.common.Money;
import com.example.payment_service.event.RefundRequestFailed;
import com.example.payment_service.event.RefundRequestProcessed;
import com.example.payment_service.model.Payment;
import com.example.payment_service.model.Refund;
import com.example.payment_service.publisher.EventPublisher;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.repository.RefundRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class RefundService {

    @Value("#{kafkaTopics.refundProcessed}")
    private String refundProcessedTopic;

    @Value("#{kafkaTopics.refundFailed}")
    private String refundFailedTopic;

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;
    private final Random random = new Random();

    public RefundService(RefundRepository refundRepository, PaymentRepository paymentRepository, EventPublisher eventPublisher) {
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Refund initiateRefund(UUID paymentId, BigDecimal refundAmount, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new RuntimeException("Cannot refund non-completed payment");
        }

        Refund refund = Refund.builder()
                .paymentId(paymentId)
                .orderId(payment.getOrderId())
                .refundAmount(refundAmount)
                .currency(payment.getCurrency())
                .refundType(refundAmount.equals(payment.getAmount()) ? 
                    Refund.RefundType.FULL : Refund.RefundType.PARTIAL)
                .reason(reason)
                .status(Refund.RefundStatus.PENDING)
                .retryCount(0)
                .build();

        refund = refundRepository.save(refund);
        
        // Process refund immediately
        processRefund(refund.getId());
        
        return refund;
    }

    @Retry(name = "refund-processing")
    @CircuitBreaker(name = "refund-processing", fallbackMethod = "processRefundFallback")
    public void processRefund(UUID refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund not found"));

        log.info("Processing refund: {} for payment: {}", refundId, refund.getPaymentId());

        refund.setStatus(Refund.RefundStatus.PROCESSING);
        refund.setRetryCount(refund.getRetryCount() + 1);
        refundRepository.save(refund);

        // Mock refund processing with 20% failure rate
        simulateRefundProcessing();
        
        if (random.nextDouble() < 0.8) {
            handleRefundSuccess(refund);
        } else {
            throw new RuntimeException("Mock refund gateway error");
        }
    }

    private void handleRefundSuccess(Refund refund) {
        refund.setStatus(Refund.RefundStatus.COMPLETED);
        refund.setRefundTransactionId("ref_" + System.currentTimeMillis());
        refund.setProcessedAt(LocalDateTime.now());
        refundRepository.save(refund);

        // Update original payment status
        Payment payment = paymentRepository.findById(refund.getPaymentId()).orElseThrow();
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        // Publish success event using AVRO
        Money refundMoney = Money.newBuilder()
                .setAmount(refund.getRefundAmount())
                .setCurrency(refund.getCurrency())
                .build();

        RefundProcessedEvent event = RefundProcessedEvent.newBuilder()
                .setRefundId(refund.getId().toString())
                .setPaymentId(refund.getPaymentId().toString())
                .setOrderId(refund.getOrderId().toString())
                .setRefundAmount(refundMoney)
                .setRefundType(RefundType.valueOf(refund.getRefundType().name()))
                .setReason(refund.getReason())
                .setRefundTransactionId(refund.getRefundTransactionId())
                .setProcessedAt(refund.getProcessedAt().toString())
                .build();

        eventPublisher.publish(new RefundRequestProcessed(refundProcessedTopic, event, refund.getOrderId().toString()));
        
        log.info("Refund completed successfully: {}", refund.getId());
    }

    public void processRefundFallback(UUID refundId, Exception ex) {
        Refund refund = refundRepository.findById(refundId).orElseThrow();
        
        if (refund.getRetryCount() >= 3) {
            refund.setStatus(Refund.RefundStatus.FAILED);
            refund.setFailureReason("Maximum retries exceeded: " + ex.getMessage());
            refundRepository.save(refund);

            // Publish failure event using AVRO
            Money refundMoney = Money.newBuilder()
                    .setAmount(refund.getRefundAmount())
                    .setCurrency(refund.getCurrency())
                    .build();

            RefundFailedEvent event = RefundFailedEvent.newBuilder()
                    .setRefundId(refund.getId().toString())
                    .setPaymentId(refund.getPaymentId().toString())
                    .setOrderId(refund.getOrderId().toString())
                    .setRefundAmount(refundMoney)
                    .setRefundType(RefundType.valueOf(refund.getRefundType().name()))
                    .setReason(refund.getReason())
                    .setFailureReason(refund.getFailureReason())
                    .setAttemptNumber(refund.getRetryCount())
                    .setMaxAttempts(3)
                    .build();

            eventPublisher.publish(new RefundRequestFailed(refundFailedTopic, event, refund.getOrderId().toString()));
            
            log.error("Refund failed after max retries: {}", refund.getId());
        }
    }

    private void simulateRefundProcessing() {
        try {
            Thread.sleep(1000 + random.nextInt(2000)); // 1-3 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public Optional<Refund> findByPaymentId(UUID paymentId) {
        return refundRepository.findByPaymentId(paymentId);
    }
}
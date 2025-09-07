package com.example.payment_service.service;

import com.example.events.payment.PaymentFailedEvent;
import com.example.events.payment.PaymentFailureReason;
import com.example.events.payment.PaymentMethod;
import com.example.events.payment.PaymentProcessedEvent;
import com.example.events.payment.PaymentRequestEvent;
import com.example.payment_service.dto.event.PaymentFailedEventDto;
import com.example.payment_service.dto.event.PaymentProcessedEventDto;
import com.example.payment_service.event.PaymentRequestFailed;
import com.example.payment_service.event.PaymentRequestProcessed;
import com.example.payment_service.mapper.PaymentRequestMapper;
import com.example.payment_service.model.Payment;
import com.example.payment_service.publisher.EventPublisher;
import com.example.payment_service.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {
    @Value("${kafka.retry.max}")
    private int maximumNumberOfRetries;

    @Value("#{kafkaTopics.paymentProcessed}")
    private String paymentRequestProcessedTopic;

    @Value("#{kafkaTopics.paymentFailed}")
    private String paymentRequestFailedTopic;

    private final PaymentRequestMapper paymentRequestMapper;
    private final EventPublisher eventPublisher;

    private final Random random = new Random();
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository, PaymentRequestMapper paymentRequestMapper, EventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentRequestMapper = paymentRequestMapper;
        this.eventPublisher = eventPublisher;
    }

    private final Map<String, TokenInfo> successTokens = Map.of(
            "pm_card_visa_4242", new TokenInfo("visa", "4242", PaymentMethod.CREDIT_CARD),
            "pm_card_mastercard_5555", new TokenInfo("mastercard", "5555", PaymentMethod.CREDIT_CARD),
            "pm_card_amex_3782", new TokenInfo("amex", "3782", PaymentMethod.CREDIT_CARD),
            "pm_card_discover_6011", new TokenInfo("discover", "6011", PaymentMethod.CREDIT_CARD)
    );

    private final Map<String, PaymentFailureReason> failureTokens = Map.of(
            "pm_card_declined_4000", PaymentFailureReason.CARD_DECLINED,
            "pm_card_insufficient_4001", PaymentFailureReason.INSUFFICIENT_FUNDS,
            "pm_card_network_4002", PaymentFailureReason.PROCESSING_ERROR,
            "pm_card_expired_4003", PaymentFailureReason.EXPIRED_CARD,
            "pm_card_fraud_4004", PaymentFailureReason.FRAUD_DETECTED
    );

    @Transactional
    @Retry(name = "payment-processing")
    @CircuitBreaker(name = "payment-processing")
    public void processPayment(
            PaymentRequestEvent request
    ) {
        log.info("Processing payment for order: {}, amount: {}, token: {}",
                request.getOrderId(), request.getAmount().getAmount(), request.getPaymentMethodToken());

        Payment payment = findOrCreatePayment(request);

        try {
            if (payment.getRetryCount() == maximumNumberOfRetries) {
                handlePaymentFailure(payment, request, PaymentFailureReason.MAXIMUM_NUMBER_OF_RETRIES_REACHED);
            }

            payment.setStatus(Payment.PaymentStatus.PROCESSING);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            simulateProcessingTime();

            String token = request.getPaymentMethodToken();

            if (successTokens.containsKey(token)) {
                TokenInfo tokenInfo = successTokens.get(token);
                log.info("Payment succeeded for token: {}", token);
                handlePaymentSuccess(payment, request, tokenInfo);
            }

            if (failureTokens.containsKey(token)) {
                PaymentFailureReason reason = failureTokens.get(token);
                log.warn("Payment failed for token: {} with reason: {}", token, reason);
                handlePaymentFailure(payment, request, reason);
            }

            if (random.nextDouble() < 0.8) {
                TokenInfo defaultToken = new TokenInfo("visa", "4242", PaymentMethod.CREDIT_CARD);
                log.info("Random success for unknown token: {}", token);
                handlePaymentSuccess(payment, request, defaultToken);
            } else {
                PaymentFailureReason randomReason = getRandomFailureReason();
                log.warn("Random failure for unknown token: {} with reason: {}", token, randomReason);
                handlePaymentFailure(payment, request, randomReason);
            }

        } catch (Exception e) {
            log.error("Payment processing error for order: {}", request.getOrderId(), e);
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason("Internal processing error: " + e.getMessage());
            paymentRepository.save(payment);
        }
    }

    private Payment findOrCreatePayment(PaymentRequestEvent request) {
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(UUID.fromString(request.getOrderId()));

        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            payment.setRetryCount(-1);
            return payment;
        } else {
            Payment payment = new Payment();
            payment.setOrderId(UUID.fromString(request.getOrderId()));
            payment.setAmount(request.getAmount().getAmount());
            payment.setCurrency(request.getAmount().getCurrency());
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setPaymentMethod(mapToPaymentMethodType(request.getPaymentMethodType()));
            payment.setRetryCount(0);
            return paymentRepository.save(payment);
        }
    }

    private Payment.PaymentMethodType mapToPaymentMethodType(String paymentMethodType) {
        return switch (paymentMethodType.toLowerCase()) {
            case "paypal" -> Payment.PaymentMethodType.PAYPAL;
            case "apple_pay" -> Payment.PaymentMethodType.APPLE_PAY;
            case "google_pay" -> Payment.PaymentMethodType.GOOGLE_PAY;
            default -> Payment.PaymentMethodType.CREDIT_CARD;
        };
    }

    private void handlePaymentSuccess(Payment payment, PaymentRequestEvent request, TokenInfo tokenInfo) {
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setTransactionId("txn_" + System.currentTimeMillis());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        PaymentProcessedEventDto paymentProcessedEventDto = createSuccessEvent(request, tokenInfo, payment.getTransactionId());

        eventPublisher.publish(new PaymentRequestProcessed(paymentRequestProcessedTopic, payment.getOrderId().toString(), paymentProcessedEventDto));
    }

    private void handlePaymentFailure(
            Payment payment,
            PaymentRequestEvent request,
            PaymentFailureReason reason
    ) {
        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.setFailureReason(reason.toString());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        PaymentFailedEventDto paymentFailedEventDto = createFailureEvent(request, reason, payment.getRetryCount());

        eventPublisher.publish(new PaymentRequestFailed(paymentRequestFailedTopic, payment.getOrderId().toString(), paymentFailedEventDto));
    }

    private PaymentProcessedEventDto createSuccessEvent(
            PaymentRequestEvent request,
            TokenInfo tokenInfo,
            String transactionId
    ) {
        PaymentProcessedEvent paymentProcessedEvent = PaymentProcessedEvent.newBuilder()
                .setPaymentId("pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                .setOrderId(request.getOrderId())
                .setAmount(request.getAmount())
                .setPaymentMethod(tokenInfo.paymentMethod)
                .setTransactionId(transactionId)
                .setLastFourDigits(tokenInfo.last4)
                .setCardBrand(tokenInfo.brand)
                .build();

        return paymentRequestMapper.toPaymentProcessedEventDto(paymentProcessedEvent);
    }

    private PaymentFailedEventDto createFailureEvent(
            PaymentRequestEvent request,
            PaymentFailureReason reason,
            int retryCount
    ) {
        PaymentFailedEvent paymentFailedEvent = PaymentFailedEvent.newBuilder()
                .setOrderId(request.getOrderId())
                .setAmount(request.getAmount())
                .setFailureReason(reason)
                .setAttemptNumber(retryCount)
                .setMaxAttempts(maximumNumberOfRetries)
                .build();

        return paymentRequestMapper.toPaymentFailedEventDto(paymentFailedEvent);
    }

    private void simulateProcessingTime() {
        try {
            int delay = 1000 + random.nextInt(2000);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Payment processing interrupted");
        }
    }

    private PaymentFailureReason getRandomFailureReason() {
        PaymentFailureReason[] reasons = PaymentFailureReason.values();
        return reasons[random.nextInt(reasons.length)];
    }

    private record TokenInfo(String brand, String last4, PaymentMethod paymentMethod) {
    }
}

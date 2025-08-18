package com.example.payment_service.repository;

import com.example.payment_service.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    Optional<Payment> findByOrderId(UUID orderId);
    
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.retryCount < 3")
    List<Payment> findRetryablePayments(@Param("status") Payment.PaymentStatus status);
    
    Optional<Payment> findByTransactionId(String transactionId);
    
    @Query("SELECT p FROM Payment p WHERE p.orderId = :orderId AND p.status IN :statuses")
    Optional<Payment> findByOrderIdAndStatusIn(@Param("orderId") UUID orderId, 
                                              @Param("statuses") List<Payment.PaymentStatus> statuses);
}
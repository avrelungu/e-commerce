package com.example.payment_service.repository;

import com.example.payment_service.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {
    
    Optional<Refund> findByPaymentId(UUID paymentId);
    
    List<Refund> findByOrderId(UUID orderId);
    
    List<Refund> findByStatus(Refund.RefundStatus status);
    
    @Query("SELECT r FROM Refund r WHERE r.status = :status AND r.retryCount < 3")
    List<Refund> findRetryableRefunds(@Param("status") Refund.RefundStatus status);
}
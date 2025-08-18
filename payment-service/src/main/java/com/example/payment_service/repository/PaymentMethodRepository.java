package com.example.payment_service.repository;

import com.example.payment_service.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    
    List<PaymentMethod> findByCustomerId(UUID customerId);
    
    List<PaymentMethod> findByCustomerIdAndIsActiveTrue(UUID customerId);
    
    Optional<PaymentMethod> findByCustomerIdAndIsDefaultTrueAndIsActiveTrue(UUID customerId);
    
    Optional<PaymentMethod> findByToken(String token);
    
    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.customerId = :customerId AND pm.type = :type AND pm.isActive = true")
    List<PaymentMethod> findByCustomerIdAndType(@Param("customerId") UUID customerId, 
                                               @Param("type") PaymentMethod.PaymentMethodType type);
}
package com.example.inventory_service.repository;

import com.example.inventory_service.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {
    List<StockReservation> findByOrderId(UUID orderId);
    
    @Query("SELECT COALESCE(SUM(sr.quantity), 0) FROM StockReservation sr WHERE sr.product.id = :productId AND sr.status = 'RESERVED'")
    Integer getTotalReservedQuantityByProductId(@Param("productId") UUID productId);
    
    List<StockReservation> findByStatusAndExpiresAtBefore(String status, LocalDateTime expirationTime);
}

package com.example.inventory_service.repository;

import com.example.inventory_service.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {
}

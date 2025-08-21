package com.example.shipping_service.repository;

import com.example.shipping_service.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByOrderId(UUID orderId);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    List<Shipment> findByStatus(Shipment.ShipmentStatus status);

    @Query("SELECT s FROM Shipment s WHERE s.status IN :statuses")
    List<Shipment> findByStatusIn(@Param("statuses") List<Shipment.ShipmentStatus> statuses);

    @Query("SELECT s FROM Shipment s WHERE s.carrier = :carrier AND s.status = :status")
    List<Shipment> findByCarrierAndStatus(@Param("carrier") String carrier, @Param("status") Shipment.ShipmentStatus status);
}
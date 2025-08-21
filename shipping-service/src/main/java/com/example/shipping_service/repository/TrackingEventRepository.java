package com.example.shipping_service.repository;

import com.example.shipping_service.model.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, UUID> {

    @Query("SELECT te FROM TrackingEvent te WHERE te.shipmentId = :shipmentId ORDER BY te.eventTime DESC")
    List<TrackingEvent> findByShipmentIdOrderByEventTimeDesc(@Param("shipmentId") UUID shipmentId);

    @Query("SELECT te FROM TrackingEvent te WHERE te.shipmentId = :shipmentId ORDER BY te.eventTime ASC")
    List<TrackingEvent> findByShipmentIdOrderByEventTimeAsc(@Param("shipmentId") UUID shipmentId);
}
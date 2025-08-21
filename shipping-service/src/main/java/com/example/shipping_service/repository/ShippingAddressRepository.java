package com.example.shipping_service.repository;

import com.example.shipping_service.model.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, UUID> {

    Optional<ShippingAddress> findByShipmentId(UUID shipmentId);
}
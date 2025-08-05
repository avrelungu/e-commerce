package com.example.inventory_service.repository;

import com.example.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByProductId(UUID productId);

    List<Inventory> findAllByProductIdIn(List<UUID> productIds);
}

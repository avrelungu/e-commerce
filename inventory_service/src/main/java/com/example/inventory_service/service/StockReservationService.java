package com.example.inventory_service.service;

import com.example.inventory_service.dto.ReservationRequestDto;
import com.example.inventory_service.model.Inventory;
import com.example.inventory_service.model.Product;
import com.example.inventory_service.model.StockReservation;
import com.example.inventory_service.repository.InventoryRepository;
import com.example.inventory_service.repository.ProductRepository;
import com.example.inventory_service.repository.StockReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class StockReservationService {
    
    private final StockReservationRepository stockReservationRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    
    public StockReservationService(
            StockReservationRepository stockReservationRepository,
            InventoryRepository inventoryRepository,
            ProductRepository productRepository) {
        this.stockReservationRepository = stockReservationRepository;
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }
    
    @Transactional
    public boolean reserveStock(UUID orderId, List<ReservationRequestDto> reservationRequests) {
        log.info("Attempting to reserve stock for order: {}", orderId);
        
        try {
            for (ReservationRequestDto request : reservationRequests) {
                if (!hasAvailableStock(request.getProductId(), request.getQuantity())) {
                    log.warn("Insufficient stock for product {} - requested: {}, available: {}", 
                            request.getProductId(), request.getQuantity(), getAvailableStock(request.getProductId()));
                    return false;
                }
            }
            
            for (ReservationRequestDto request : reservationRequests) {
                createReservation(orderId, request.getProductId(), request.getQuantity());
                updateInventoryReservedQuantity(request.getProductId(), request.getQuantity());
            }
            
            log.info("Successfully reserved stock for order: {}", orderId);
            return true;
            
        } catch (Exception e) {
            log.error("Error reserving stock for order: {}", orderId, e);
            throw e;
        }
    }
    
    private boolean hasAvailableStock(UUID productId, int requestedQuantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(null);
        
        if (inventory == null) {
            log.warn("No inventory found for product: {}", productId);
            return false;
        }
        
        int currentlyReserved = stockReservationRepository.getTotalReservedQuantityByProductId(productId);
        int availableForReservation = inventory.getAvailableQuantity() - currentlyReserved;
        
        return availableForReservation >= requestedQuantity;
    }
    
    private int getAvailableStock(UUID productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(null);
        
        if (inventory == null) {
            return 0;
        }
        
        int currentlyReserved = stockReservationRepository.getTotalReservedQuantityByProductId(productId);
        return inventory.getAvailableQuantity() - currentlyReserved;
    }
    
    private void createReservation(UUID orderId, UUID productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        
        StockReservation reservation = new StockReservation();
        reservation.setOrderId(orderId);
        reservation.setProduct(product);
        reservation.setQuantity(quantity);
        reservation.setStatus("RESERVED");
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        reservation.setCreatedAt(LocalDateTime.now());
        
        stockReservationRepository.save(reservation);
        log.info("Created stock reservation for order: {}, product: {}, quantity: {}", 
                orderId, productId, quantity);
    }
    
    private void updateInventoryReservedQuantity(UUID productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventory.setLastUpdated(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }
    
    @Transactional
    public void confirmReservation(UUID orderId) {
        List<StockReservation> reservations = stockReservationRepository.findByOrderId(orderId);
        
        for (StockReservation reservation : reservations) {
            if ("RESERVED".equals(reservation.getStatus())) {
                reservation.setStatus("CONFIRMED");
                stockReservationRepository.save(reservation);
                
                Inventory inventory = inventoryRepository.findByProductId(reservation.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Inventory not found"));
                
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() - reservation.getQuantity());
                inventory.setReservedQuantity(inventory.getReservedQuantity() - reservation.getQuantity());
                inventory.setLastUpdated(LocalDateTime.now());
                inventoryRepository.save(inventory);
                
                log.info("Confirmed stock reservation for order: {}, product: {}", 
                        orderId, reservation.getProduct().getId());
            }
        }
    }
    
    @Transactional
    public void releaseReservation(UUID orderId) {
        List<StockReservation> reservations = stockReservationRepository.findByOrderId(orderId);
        
        for (StockReservation reservation : reservations) {
            if ("RESERVED".equals(reservation.getStatus())) {
                reservation.setStatus("RELEASED");
                stockReservationRepository.save(reservation);
                
                Inventory inventory = inventoryRepository.findByProductId(reservation.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Inventory not found"));
                
                inventory.setReservedQuantity(inventory.getReservedQuantity() - reservation.getQuantity());
                inventory.setLastUpdated(LocalDateTime.now());
                inventoryRepository.save(inventory);
                
                log.info("Released stock reservation for order: {}, product: {}", 
                        orderId, reservation.getProduct().getId());
            }
        }
    }
}

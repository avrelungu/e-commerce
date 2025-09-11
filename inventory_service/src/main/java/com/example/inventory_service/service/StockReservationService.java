package com.example.inventory_service.service;

import com.example.events.inventory.OutOfStockEvent;
import com.example.inventory_service.dto.ReservationRequestDto;
import com.example.inventory_service.event.OutOfStock;
import com.example.inventory_service.exception.InsufficientStockException;
import com.example.inventory_service.mapper.StockReservationMapper;
import com.example.inventory_service.model.Inventory;
import com.example.inventory_service.model.Product;
import com.example.inventory_service.model.StockReservation;
import com.example.inventory_service.publisher.EventPublisher;
import com.example.inventory_service.repository.InventoryRepository;
import com.example.inventory_service.repository.ProductRepository;
import com.example.inventory_service.repository.StockReservationRepository;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class StockReservationService {
    @Value("#{kafkaTopics.outOfStock}")
    private String outOfStockTopic;

    private final StockReservationRepository stockReservationRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StockReservationMapper stockReservationMapper;
    private final EventPublisher eventPublisher;
    private final StockAlertService stockAlertService;

    public StockReservationService(
            StockReservationRepository stockReservationRepository,
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            StockReservationMapper stockReservationMapper,
            EventPublisher eventPublisher,
            StockAlertService stockAlertService
    ) {
        this.stockReservationRepository = stockReservationRepository;
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.stockReservationMapper = stockReservationMapper;
        this.eventPublisher = eventPublisher;
        this.stockAlertService = stockAlertService;
    }

    @Retry(name = "inventory-stock-reservation")
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<StockReservation> reserveStock(UUID orderId, List<ReservationRequestDto> reservationRequests) throws InsufficientStockException {
        log.info("Attempting to reserve stock for order: {}", orderId);

        try {
            for (ReservationRequestDto request : reservationRequests) {
                if (!hasAvailableStock(request.getProductId(), request.getQuantity())) {
                    log.warn("Insufficient stock for product {} - requested: {}, available: {}",
                            request.getProductId(), request.getQuantity(), getAvailableStock(request.getProductId()));

                    OutOfStockEvent outOfStockEvent = stockReservationMapper.toOutOfStockEvent(request, orderId, getAvailableStock(request.getProductId()));

                    eventPublisher.publish(new OutOfStock(outOfStockTopic, outOfStockEvent, String.valueOf(orderId)));

                    log.error("Insufficient stock for product {}", request.getProductId() );

                    return new ArrayList<>();
                }
            }

            List<StockReservation> createdReservations = new ArrayList<>();
            for (ReservationRequestDto request : reservationRequests) {
                StockReservation reservation = createReservation(orderId, request.getProductId(), request.getQuantity());
                createdReservations.add(reservation);
                updateInventoryReservedQuantity(request.getProductId(), request.getQuantity());
                
                // Check for low stock after reservation
                Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + request.getProductId()));
                Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + request.getProductId()));
                
                stockAlertService.checkAndAlertLowStock(product, inventory);
            }

            log.info("Successfully reserved stock for order: {}", orderId);
            return createdReservations;

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

    private StockReservation createReservation(UUID orderId, UUID productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        StockReservation reservation = new StockReservation();
        reservation.setOrderId(orderId);
        reservation.setProduct(product);
        reservation.setQuantity(quantity);
        reservation.setStatus("RESERVED");
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        reservation.setCreatedAt(LocalDateTime.now());

        StockReservation savedReservation = stockReservationRepository.save(reservation);
        log.info("Created stock reservation for order: {}, product: {}, quantity: {}",
                orderId, productId, quantity);

        return savedReservation;
    }

    private void updateInventoryReservedQuantity(UUID productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));

        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventory.setLastUpdated(LocalDateTime.now());
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void confirmReservation(UUID orderId) throws InsufficientStockException {
        List<StockReservation> reservations = stockReservationRepository.findByOrderId(orderId);

        for (StockReservation reservation : reservations) {
            if ("RESERVED".equals(reservation.getStatus())) {
                reservation.setStatus("CONFIRMED");
                stockReservationRepository.save(reservation);

                Inventory inventory = inventoryRepository.findByProductId(reservation.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Inventory not found"));

                if (inventory.getAvailableQuantity() < reservation.getQuantity()) {
                    throw new InsufficientStockException("Stock is no longer available for reservation " + reservation.getId(), HttpStatus.BAD_REQUEST);
                }

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

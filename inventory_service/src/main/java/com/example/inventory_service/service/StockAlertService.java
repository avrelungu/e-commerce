package com.example.inventory_service.service;

import com.example.events.inventory.LowStockAlertEvent;
import com.example.inventory_service.event.LowStockAlert;
import com.example.inventory_service.model.Inventory;
import com.example.inventory_service.model.Product;
import com.example.inventory_service.publisher.EventPublisher;
import com.example.shared_common.idempotency.EventIdempotencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StockAlertService {
    
    @Value("#{kafkaTopics.lowStockAlert}")
    private String lowStockAlertTopic;
    
    private final EventPublisher eventPublisher;
    private final EventIdempotencyService idempotencyService;
    
    public StockAlertService(EventPublisher eventPublisher, EventIdempotencyService idempotencyService) {
        this.eventPublisher = eventPublisher;
        this.idempotencyService = idempotencyService;
    }
    
    public void checkAndAlertLowStock(Product product, Inventory inventory) {
        int currentQuantity = inventory.getAvailableQuantity();
        int threshold = product.getLowStockThreshold();
        
        if (currentQuantity <= threshold) {
            String alertKey = "low-stock-alert-" + product.getSku() + "-" + currentQuantity;
            
            boolean processed = idempotencyService.processOnce(
                alertKey,
                () -> {
                    LowStockAlertEvent alertEvent = new LowStockAlertEvent(
                        product.getId().toString(),
                        product.getSku(),
                        product.getName(),
                        currentQuantity,
                        threshold,
                        System.currentTimeMillis()
                    );
                    
                    eventPublisher.publish(new LowStockAlert(
                        lowStockAlertTopic, 
                        alertEvent, 
                        product.getSku()
                    ));
                    
                    log.warn("Low stock alert sent for product: {} (SKU: {}), quantity: {}, threshold: {}", 
                        product.getName(), product.getSku(), currentQuantity, threshold);
                }
            );
            
            if (!processed) {
                log.debug("Low stock alert already sent for product: {} at quantity: {}", 
                    product.getSku(), currentQuantity);
            }
        } else {
            log.debug("Stock level OK for product: {} (SKU: {}), quantity: {}, threshold: {}", 
                product.getName(), product.getSku(), currentQuantity, threshold);
        }
    }
}
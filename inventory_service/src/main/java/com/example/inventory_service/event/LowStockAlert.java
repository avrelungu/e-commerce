package com.example.inventory_service.event;

import com.example.events.inventory.LowStockAlertEvent;

public class LowStockAlert extends DomainEvent {
    private final String lowStockAlertTopic;

    public LowStockAlert(String lowStockAlertTopic, LowStockAlertEvent lowStockAlertEvent, String productSku) {
        super(lowStockAlertEvent, productSku);

        this.lowStockAlertTopic = lowStockAlertTopic;
    }

    @Override
    public String getEventType() {
        return lowStockAlertTopic;
    }

    @Override
    public String getTopic() {
        return lowStockAlertTopic;
    }
}
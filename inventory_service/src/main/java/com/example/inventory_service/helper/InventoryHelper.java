package com.example.inventory_service.helper;

import com.example.inventory_service.model.Inventory;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class InventoryHelper {

    @Named("hasStock")
    public boolean hasStock(Inventory inventory, Integer requestedQuantity) {
        if (inventory == null || requestedQuantity == null || requestedQuantity <= 0) {
            return false;
        }
        
        return inventory.getAvailableQuantity() - inventory.getReservedQuantity() >= requestedQuantity;
    }
}

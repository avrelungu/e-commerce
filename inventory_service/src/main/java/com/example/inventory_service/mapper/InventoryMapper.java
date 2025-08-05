package com.example.inventory_service.mapper;

import com.example.inventory_service.dto.OrderItemInventoryCheckDto;
import com.example.inventory_service.helper.InventoryHelper;
import com.example.inventory_service.model.Inventory;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    @Mapping(target = "productId", source = "inventory.product.id")
    @Mapping(target = "name", source = "inventory.product.name")
    @Mapping(target = "price", source = "inventory.product.price")
    @Mapping(target = "available", ignore = true)
    OrderItemInventoryCheckDto toOrderItemInventoryCheckDto(Inventory inventory, Integer requestedQuantity);

    @AfterMapping
    default void setAvailable(@MappingTarget OrderItemInventoryCheckDto dto, Inventory inventory, Integer requestedQuantity) {
        dto.setAvailable(hasStock(inventory, requestedQuantity));
        dto.setTotalPrice(totalPrice(inventory, requestedQuantity));
        dto.setQuantity(requestedQuantity);
    }

    @Named("hasStock")
    static boolean hasStock(Inventory inventory, Integer requestedQuantity) {
        if (inventory == null || requestedQuantity == null || requestedQuantity <= 0) {
            return false;
        }

        return inventory.getAvailableQuantity() - inventory.getReservedQuantity() >= requestedQuantity;
    }

    @Named("totalPrice")
    static BigDecimal totalPrice(Inventory inventory, Integer requestedQuantity) {
        return inventory.getProduct().getPrice().multiply(BigDecimal.valueOf(requestedQuantity));
    }
}

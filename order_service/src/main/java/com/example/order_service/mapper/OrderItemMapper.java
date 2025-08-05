package com.example.order_service.mapper;

import com.example.order_service.dto.OrderItemDto;
import com.example.order_service.dto.OrderItemsAvailableInventoryDto;
import com.example.order_service.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "productName", source = "name")
    @Mapping(target = "unitPrice", source = "price")
    @Mapping(target = "quantity", source = "quantity")
    OrderItem toOrderItem(OrderItemsAvailableInventoryDto orderItemsAvailableInventoryDto);

    OrderItemDto toOrderItemDto(OrderItem orderItem);
}

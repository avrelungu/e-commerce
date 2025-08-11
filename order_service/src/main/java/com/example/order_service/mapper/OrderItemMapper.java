package com.example.order_service.mapper;

import com.example.order_service.dto.OrderItemDto;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItemDto toOrderItemDto(OrderItem orderItem);

    @Mapping(target = "order", source = "order")
    OrderItem toOrderItem(OrderItemDto orderItemDto, Order order);

    List<OrderItem> toOrderItemList(List<OrderItemDto> orderDto);
}

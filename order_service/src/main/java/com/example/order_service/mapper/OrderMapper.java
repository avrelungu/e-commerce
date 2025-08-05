package com.example.order_service.mapper;

import com.example.order_service.dto.CreateOrderDto;
import com.example.order_service.dto.OrderDto;
import com.example.order_service.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toOrderFromCreateOrderDto(CreateOrderDto orderDto);

    @Mapping(target = "id", ignore = true)
    OrderDto toOrderDto(Order order);
}

package com.example.order_service.mapper;

import com.example.events.common.Address;
import com.example.events.common.Money;
import com.example.events.order.OrderCreatedEvent;
import com.example.events.order.OrderItem;
import com.example.order_service.dto.AddressDto;
import com.example.order_service.model.Order;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderEventMapper {
    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "timestamp", source = "createdAt")
    @Mapping(target = "items", source = "orderItems")
    OrderCreatedEvent toOrderCreatedEvent(Order order);

    default Money map(BigDecimal value) {
        return Money.newBuilder()
                .setAmount(value.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    default BigDecimal map(Money money) {
        return new BigDecimal(String.valueOf(money.getAmount()));
    }

    default Address map(JsonNode address) {
        ObjectMapper mapper = new ObjectMapper();
        AddressDto addressDto = mapper.convertValue(address, AddressDto.class);

        return Address.newBuilder()
                .setZipCode(addressDto.getZipCode())
                .setState(addressDto.getState())
                .setCountry(addressDto.getCountry())
                .setCity(addressDto.getCity())
                .setStreet(addressDto.getStreet())
                .build();
    }

    default long map(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    default List<OrderItem> map(List<com.example.order_service.model.OrderItem> orderItems) {
        return orderItems.stream()
                .map(orderItem -> OrderItem.newBuilder()
                        .setProductId(String.valueOf(orderItem.getProductId()))
                        .setProductName(orderItem.getProductName())
                        .setQuantity(orderItem.getQuantity())
                        .setTotalPrice(map(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()))))
                        .setUnitPrice(map(orderItem.getUnitPrice()))
                        .build())
                .toList();
    }
}

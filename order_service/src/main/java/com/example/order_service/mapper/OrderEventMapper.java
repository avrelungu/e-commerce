package com.example.order_service.mapper;

import com.example.events.common.Address;
import com.example.events.common.Money;
import com.example.events.order.OrderCreatedEvent;
import com.example.events.order.OrderItem;
import com.example.order_service.dto.AddressDto;
import com.example.order_service.dto.event.MoneyDto;
import com.example.order_service.dto.event.OrderCreatedEventDto;
import com.example.order_service.dto.event.OrderItemDto;
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
        return money.getAmount();
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

    // Convert Avro objects to DTOs for JSON serialization
    default OrderCreatedEventDto toDto(OrderCreatedEvent avroEvent) {
        OrderCreatedEventDto dto = new OrderCreatedEventDto();
        dto.setTimestamp(avroEvent.getTimestamp());
        dto.setOrderId(avroEvent.getOrderId());
        dto.setOrderNumber(avroEvent.getOrderNumber());
        dto.setCustomerId(avroEvent.getCustomerId());
        dto.setItems(avroEvent.getItems().stream().map(this::toDto).toList());
        dto.setTotalAmount(toDto(avroEvent.getTotalAmount()));
        dto.setTaxAmount(toDto(avroEvent.getTaxAmount()));
        dto.setShippingAmount(toDto(avroEvent.getShippingAmount()));
        dto.setShippingAddress(toDtoAddress(avroEvent.getShippingAddress()));
        return dto;
    }

    default OrderItemDto toDto(OrderItem avroItem) {
        OrderItemDto dto = new OrderItemDto();
        dto.setProductId(avroItem.getProductId());
        dto.setProductName(avroItem.getProductName());
        dto.setQuantity(avroItem.getQuantity());
        dto.setUnitPrice(toDto(avroItem.getUnitPrice()));
        dto.setTotalPrice(toDto(avroItem.getTotalPrice()));
        return dto;
    }

    default MoneyDto toDto(Money avroMoney) {
        return new MoneyDto(avroMoney.getAmount(), avroMoney.getCurrency());
    }

    default com.example.order_service.dto.event.AddressDto toDtoAddress(Address avroAddress) {
        com.example.order_service.dto.event.AddressDto dto = new com.example.order_service.dto.event.AddressDto();
        dto.setStreet(avroAddress.getStreet());
        dto.setCity(avroAddress.getCity());
        dto.setState(avroAddress.getState());
        dto.setZipCode(avroAddress.getZipCode());
        dto.setCountry(avroAddress.getCountry());
        return dto;
    }
}

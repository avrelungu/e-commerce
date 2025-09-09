package com.example.inventory_service.mapper;

import com.example.events.inventory.OutOfStockEvent;
import com.example.events.inventory.StockReservedEvent;
import com.example.inventory_service.dto.ReservationRequestDto;
import com.example.inventory_service.dto.StockReservationDto;
import com.example.inventory_service.model.StockReservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface StockReservationMapper {
    StockReservationDto toStockReservationDto(StockReservation stockReservation);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "reservationId", source = "id")
    com.example.events.inventory.StockReservation toStockReservation(StockReservation stockReservation);

    default StockReservedEvent toStockReservedEvent(List<StockReservation> stockReservations, String orderId) {
        if (stockReservations == null || stockReservations.isEmpty()) {
            return null;
        }

        return StockReservedEvent.newBuilder()
                .setOrderId(orderId)
                .setReservations(stockReservations.stream().map(this::toStockReservation).toList())
                .build();
    }

    default OutOfStockEvent toOutOfStockEvent(ReservationRequestDto request, UUID orderId, int availableQuantity) {
        if (null == request) {
            return null;
        }

        return OutOfStockEvent.newBuilder()
                .setOrderId(String.valueOf(orderId))
                .setAvailableQuantity(availableQuantity)
                .setRequestedQuantity(request.getQuantity())
                .setProductId(String.valueOf(request.getProductId()))
                .build();
    }

    default long map(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}

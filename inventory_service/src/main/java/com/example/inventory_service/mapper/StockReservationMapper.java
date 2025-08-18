package com.example.inventory_service.mapper;

import com.example.events.inventory.StockReservedEvent;
import com.example.inventory_service.dto.StockReservationDto;
import com.example.inventory_service.dto.event.StockReservedEventDto;
import com.example.inventory_service.model.StockReservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface StockReservationMapper {
    StockReservationDto toStockReservationDto(StockReservation stockReservation);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "reservationId", source = "id")
    com.example.events.inventory.StockReservation toStockReservation(StockReservation stockReservation);

    default StockReservedEvent toStockReservedEvent(List<StockReservation> stockReservations, String eventId, String orderId) {
        if (stockReservations == null || stockReservations.isEmpty()) {
            return null;
        }

        return StockReservedEvent.newBuilder()
                .setEventId(eventId)
                .setOrderId(orderId)
                .setAggregateId(orderId)
                .setReservations(stockReservations.stream().map(this::toStockReservation).toList())
                .setTimestamp(map(LocalDateTime.now()))
                .build();
    };

    default long map(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    StockReservedEventDto toStockReservedEventDto(StockReservedEvent stockReservationDto);
}

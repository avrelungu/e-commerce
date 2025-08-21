package com.example.shipping_service.controller;

import com.example.shipping_service.dto.ShipmentDto;
import com.example.shipping_service.dto.TrackingEventDto;
import com.example.shipping_service.dto.TrackingUpdateDto;
import com.example.shipping_service.model.Shipment;
import com.example.shipping_service.model.TrackingEvent;
import com.example.shipping_service.service.ShippingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/shipping")
@Slf4j
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ShipmentDto> getShipmentByOrderId(@PathVariable UUID orderId) {
        log.info("Getting shipment for order: {}", orderId);
        
        Optional<Shipment> shipmentOpt = shippingService.findByOrderId(orderId);
        if (shipmentOpt.isPresent()) {
            ShipmentDto shipmentDto = convertToDto(shipmentOpt.get());
            return ResponseEntity.ok(shipmentDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentDto> getShipmentByTrackingNumber(@PathVariable String trackingNumber) {
        log.info("Getting shipment for tracking number: {}", trackingNumber);
        
        Optional<Shipment> shipmentOpt = shippingService.findByTrackingNumber(trackingNumber);
        if (shipmentOpt.isPresent()) {
            ShipmentDto shipmentDto = convertToDto(shipmentOpt.get());
            return ResponseEntity.ok(shipmentDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/tracking/{trackingNumber}/events")
    public ResponseEntity<List<TrackingEventDto>> getTrackingEvents(@PathVariable String trackingNumber) {
        log.info("Getting tracking events for tracking number: {}", trackingNumber);
        
        Optional<Shipment> shipmentOpt = shippingService.findByTrackingNumber(trackingNumber);
        if (shipmentOpt.isPresent()) {
            List<TrackingEvent> events = shippingService.getTrackingHistory(shipmentOpt.get().getId());
            List<TrackingEventDto> eventDtos = events.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(eventDtos);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/tracking/update")
    public ResponseEntity<Void> updateTracking(@RequestBody TrackingUpdateDto updateDto) {
        log.info("Updating tracking for: {}", updateDto.getTrackingNumber());
        
        try {
            shippingService.updateTrackingStatus(
                updateDto.getTrackingNumber(),
                Shipment.ShipmentStatus.IN_TRANSIT,
                updateDto.getLocation(),
                updateDto.getDescription()
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating tracking for {}: {}", updateDto.getTrackingNumber(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    private ShipmentDto convertToDto(Shipment shipment) {
        ShipmentDto dto = new ShipmentDto();
        dto.setId(shipment.getId());
        dto.setOrderId(shipment.getOrderId());
        dto.setTrackingNumber(shipment.getTrackingNumber());
        dto.setCarrier(shipment.getCarrier());
        dto.setStatus(shipment.getStatus());
        dto.setShippingMethod(shipment.getShippingMethod());
        dto.setShippingCost(shipment.getShippingCost());
        dto.setEstimatedDelivery(shipment.getEstimatedDelivery());
        dto.setActualDelivery(shipment.getActualDelivery());
        dto.setLabelUrl(shipment.getLabelUrl());
        dto.setCreatedAt(shipment.getCreatedAt());
        dto.setUpdatedAt(shipment.getUpdatedAt());
        return dto;
    }

    private TrackingEventDto convertToDto(TrackingEvent event) {
        TrackingEventDto dto = new TrackingEventDto();
        dto.setId(event.getId());
        dto.setShipmentId(event.getShipmentId());
        dto.setStatus(event.getStatus());
        dto.setLocation(event.getLocation());
        dto.setDescription(event.getDescription());
        dto.setEventTime(event.getEventTime());
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }
}
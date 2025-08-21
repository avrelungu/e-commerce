package com.example.shipping_service.service;

import com.example.shipping_service.dto.event.OrderDeliveredEventDto;
import com.example.shipping_service.dto.event.OrderShippedEventDto;
import com.example.shipping_service.dto.event.ShipmentTrackingUpdateEventDto;
import com.example.shipping_service.event.OrderDelivered;
import com.example.shipping_service.event.OrderShipped;
import com.example.shipping_service.event.ShipmentTrackingUpdate;
import com.example.shipping_service.model.Shipment;
import com.example.shipping_service.model.ShippingAddress;
import com.example.shipping_service.model.TrackingEvent;
import com.example.shipping_service.publisher.EventPublisher;
import com.example.shipping_service.repository.ShipmentRepository;
import com.example.shipping_service.repository.ShippingAddressRepository;
import com.example.shipping_service.repository.TrackingEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class ShippingService {

    @Value("${kafka.topics.order-shipped}")
    private String orderShippedTopic;

    @Value("${kafka.topics.order-delivered}")
    private String orderDeliveredTopic;

    @Value("${kafka.topics.shipment-tracking-update}")
    private String trackingUpdateTopic;

    private final ShipmentRepository shipmentRepository;
    private final ShippingAddressRepository addressRepository;
    private final TrackingEventRepository trackingEventRepository;
    private final EventPublisher eventPublisher;

    private final Random random = new Random();
    private final List<String> carriers = Arrays.asList("FedEx", "UPS", "USPS", "DHL");
    private final List<String> shippingMethods = Arrays.asList("Standard", "Express", "Overnight");
    private final List<String> locations = Arrays.asList(
            "Memphis, TN", "Louisville, KY", "Atlanta, GA", "Chicago, IL",
            "Los Angeles, CA", "Phoenix, AZ", "Newark, NJ", "Oakland, CA"
    );

    public ShippingService(
            ShipmentRepository shipmentRepository,
            ShippingAddressRepository addressRepository,
            TrackingEventRepository trackingEventRepository,
            EventPublisher eventPublisher
    ) {
        this.shipmentRepository = shipmentRepository;
        this.addressRepository = addressRepository;
        this.trackingEventRepository = trackingEventRepository;
        this.eventPublisher = eventPublisher;
    }

    public Shipment createShipment(UUID orderId, JsonNode shippingAddress) {
        log.info("Creating shipment for order: {}", orderId);

        Shipment shipment = new Shipment();
        shipment.setOrderId(orderId);
        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setCarrier(selectRandomCarrier());
        shipment.setStatus(Shipment.ShipmentStatus.PENDING);
        shipment.setShippingMethod(selectRandomShippingMethod());
        shipment.setShippingCost(calculateShippingCost(shipment.getShippingMethod()));
        shipment.setEstimatedDelivery(calculateEstimatedDelivery(shipment.getShippingMethod()));

        Shipment savedShipment = shipmentRepository.save(shipment);

        ShippingAddress address = new ShippingAddress();
        address.setShipmentId(savedShipment.getId());
        address.setRecipientName("Customer Name");
        address.setAddressData(shippingAddress);
        addressRepository.save(address);

        createTrackingEvent(savedShipment.getId(), Shipment.ShipmentStatus.PENDING,
                "Order Center", "Shipment created and awaiting processing");

        generateShippingLabel(savedShipment);

        return savedShipment;
    }

    public void generateShippingLabel(Shipment shipment) {
        log.info("Generating shipping label for shipment: {}", shipment.getId());

        String labelUrl = "https://shipping-labels.example.com/label/" + shipment.getTrackingNumber() + ".pdf";
        shipment.setLabelUrl(labelUrl);
        shipment.setStatus(Shipment.ShipmentStatus.LABEL_CREATED);

        Shipment updatedShipment = shipmentRepository.save(shipment);

        createTrackingEvent(shipment.getId(), Shipment.ShipmentStatus.LABEL_CREATED,
                "Shipping Facility", "Shipping label created and ready for pickup");

        publishOrderShippedEvent(updatedShipment);
    }

    public Optional<Shipment> findByOrderId(UUID orderId) {
        return shipmentRepository.findByOrderId(orderId);
    }

    public Optional<Shipment> findByTrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber);
    }

    public List<TrackingEvent> getTrackingHistory(UUID shipmentId) {
        return trackingEventRepository.findByShipmentIdOrderByEventTimeAsc(shipmentId);
    }

    public void updateTrackingStatus(String trackingNumber, Shipment.ShipmentStatus newStatus,
                                     String location, String description) {
        Optional<Shipment> shipmentOpt = shipmentRepository.findByTrackingNumber(trackingNumber);
        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            shipment.setStatus(newStatus);

            if (newStatus == Shipment.ShipmentStatus.DELIVERED) {
                shipment.setActualDelivery(LocalDateTime.now());
            }

            shipmentRepository.save(shipment);

            createTrackingEvent(shipment.getId(), newStatus, location, description);

            publishTrackingUpdateEvent(shipment, newStatus, location, description);

            if (newStatus == Shipment.ShipmentStatus.DELIVERED) {
                publishOrderDeliveredEvent(shipment, location);
            }
        }
    }

    private void createTrackingEvent(UUID shipmentId, Shipment.ShipmentStatus status,
                                     String location, String description) {
        TrackingEvent event = new TrackingEvent();
        event.setShipmentId(shipmentId);
        event.setStatus(status);
        event.setLocation(location);
        event.setDescription(description);
        event.setEventTime(LocalDateTime.now());

        trackingEventRepository.save(event);
        log.info("Created tracking event for shipment {}: {} at {}", shipmentId, status, location);
    }

    private void publishOrderShippedEvent(Shipment shipment) {
        OrderShippedEventDto eventDto = new OrderShippedEventDto(
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getTrackingNumber(),
                shipment.getCarrier(),
                shipment.getShippingMethod(),
                shipment.getEstimatedDelivery(),
                shipment.getLabelUrl(),
                LocalDateTime.now()
        );

        OrderShipped event = new OrderShipped(orderShippedTopic, eventDto, shipment.getOrderId().toString());
        eventPublisher.publish(event);
        log.info("Published OrderShipped event for order: {}", shipment.getOrderId());
    }

    private void publishTrackingUpdateEvent(Shipment shipment, Shipment.ShipmentStatus status,
                                            String location, String description) {
        ShipmentTrackingUpdateEventDto eventDto = new ShipmentTrackingUpdateEventDto(
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getTrackingNumber(),
                status,
                location,
                description,
                LocalDateTime.now()
        );

        ShipmentTrackingUpdate event = new ShipmentTrackingUpdate(trackingUpdateTopic, eventDto,
                shipment.getOrderId().toString());
        eventPublisher.publish(event);
        log.info("Published tracking update event for shipment: {}", shipment.getId());
    }

    private void publishOrderDeliveredEvent(Shipment shipment, String deliveryLocation) {
        OrderDeliveredEventDto eventDto = new OrderDeliveredEventDto(
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getTrackingNumber(),
                LocalDateTime.now(),
                deliveryLocation
        );

        OrderDelivered event = new OrderDelivered(orderDeliveredTopic, eventDto, shipment.getOrderId().toString());
        eventPublisher.publish(event);
        log.info("Published OrderDelivered event for order: {}", shipment.getOrderId());
    }

    @Scheduled(fixedDelay = 30000)
    public void simulateShipmentProgress() {
        List<Shipment.ShipmentStatus> activeStatuses = Arrays.asList(
                Shipment.ShipmentStatus.LABEL_CREATED,
                Shipment.ShipmentStatus.PICKED_UP,
                Shipment.ShipmentStatus.IN_TRANSIT,
                Shipment.ShipmentStatus.OUT_FOR_DELIVERY
        );

        List<Shipment> activeShipments = shipmentRepository.findByStatusIn(activeStatuses);

        for (Shipment shipment : activeShipments) {
            if (random.nextDouble() < 0.3) {
                Shipment.ShipmentStatus nextStatus = getNextStatus(shipment.getStatus());
                if (nextStatus != null) {
                    String location = selectRandomLocation();
                    String description = getStatusDescription(nextStatus, location);
                    updateTrackingStatus(shipment.getTrackingNumber(), nextStatus, location, description);
                }
            }
        }
    }

    private String generateTrackingNumber() {
        return "SHIP" + System.currentTimeMillis() + random.nextInt(1000);
    }

    private String selectRandomCarrier() {
        return carriers.get(random.nextInt(carriers.size()));
    }

    private String selectRandomShippingMethod() {
        return shippingMethods.get(random.nextInt(shippingMethods.size()));
    }

    private String selectRandomLocation() {
        return locations.get(random.nextInt(locations.size()));
    }

    private BigDecimal calculateShippingCost(String shippingMethod) {
        return switch (shippingMethod) {
            case "Standard" -> new BigDecimal("5.99");
            case "Express" -> new BigDecimal("12.99");
            case "Overnight" -> new BigDecimal("24.99");
            default -> new BigDecimal("5.99");
        };
    }

    private LocalDateTime calculateEstimatedDelivery(String shippingMethod) {
        return switch (shippingMethod) {
            case "Standard" -> LocalDateTime.now().plusDays(5);
            case "Express" -> LocalDateTime.now().plusDays(2);
            case "Overnight" -> LocalDateTime.now().plusDays(1);
            default -> LocalDateTime.now().plusDays(5);
        };
    }

    private Shipment.ShipmentStatus getNextStatus(Shipment.ShipmentStatus currentStatus) {
        return switch (currentStatus) {
            case LABEL_CREATED -> Shipment.ShipmentStatus.PICKED_UP;
            case PICKED_UP -> Shipment.ShipmentStatus.IN_TRANSIT;
            case IN_TRANSIT -> Shipment.ShipmentStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> Shipment.ShipmentStatus.DELIVERED;
            default -> null;
        };
    }

    private String getStatusDescription(Shipment.ShipmentStatus status, String location) {
        return switch (status) {
            case PICKED_UP -> "Package picked up by " + selectRandomCarrier();
            case IN_TRANSIT -> "Package in transit at " + location;
            case OUT_FOR_DELIVERY -> "Package out for delivery in " + location;
            case DELIVERED -> "Package delivered to recipient";
            default -> "Status updated";
        };
    }
}
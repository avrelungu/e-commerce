package com.example.inventory_service.service;

import com.example.inventory_service.dto.OrderItemDto;
import com.example.inventory_service.dto.OrderItemInventoryCheckDto;
import com.example.inventory_service.event.DomainEvent;
import com.example.inventory_service.mapper.InventoryMapper;
import com.example.inventory_service.model.Inventory;
import com.example.inventory_service.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryService(
            InventoryRepository inventoryRepository,
            InventoryMapper inventoryMapper) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMapper = inventoryMapper;
    }

    public List<OrderItemInventoryCheckDto> checkInventory(List<OrderItemDto> orderItemDtoList) {
        List<OrderItemInventoryCheckDto> orderItemInventoryList = new ArrayList<>();

        orderItemDtoList.forEach(orderItemDto -> {
            Optional<Inventory> inventory = inventoryRepository.findByProductId(orderItemDto.getProductId());

            inventory.ifPresent(value -> orderItemInventoryList.add(inventoryMapper.toOrderItemInventoryCheckDto(value, orderItemDto.getQuantity())));
        });

        log.info("Inventory check result: {}", orderItemInventoryList.toString());

        return orderItemInventoryList;
    }
}

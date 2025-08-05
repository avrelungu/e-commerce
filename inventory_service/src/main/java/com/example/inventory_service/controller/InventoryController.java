package com.example.inventory_service.controller;

import com.example.inventory_service.dto.OrderItemDto;
import com.example.inventory_service.dto.OrderItemInventoryCheckDto;
import com.example.inventory_service.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<List<OrderItemInventoryCheckDto>> getProductsInventory(@RequestBody List<OrderItemDto> productInventoryDto) {

        List<OrderItemInventoryCheckDto> orderItemInventoryCheckDtoList = inventoryService.checkInventory(productInventoryDto);

        return ResponseEntity.ok(orderItemInventoryCheckDtoList);
    }
}

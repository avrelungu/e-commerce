package com.example.order_service.service;

import com.example.order_service.dto.CreateOrderDto;
import com.example.order_service.dto.OrderItemsAvailableInventoryDto;
import com.example.order_service.exceptions.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class InventoryService {
    @Value("${inventory_service.api.url}")
    private String inventoryServiceUrl;

    private final RestTemplate restTemplate;

    public InventoryService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<OrderItemsAvailableInventoryDto> checkOrderItemsInventory(CreateOrderDto orderDto) throws AppException {
        try {
            return restTemplate.exchange(
                    inventoryServiceUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(orderDto.getItems()),
                    new ParameterizedTypeReference<List<OrderItemsAvailableInventoryDto>>() {}
            ).getBody();
        } catch (Exception e) {
            log.error("Failed to retrieve inventory information: {}", e.getMessage());

            throw new AppException("Failed to retrieve inventory information: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}

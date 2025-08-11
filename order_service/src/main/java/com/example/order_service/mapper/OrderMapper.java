package com.example.order_service.mapper;

import com.example.order_service.dto.AddressDto;
import com.example.order_service.dto.CreateOrderDto;
import com.example.order_service.model.Order;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order createOrderDtoToOrder(CreateOrderDto orderDto);

    default JsonNode mapAddressDtoToJsonNode(AddressDto addressDto) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(addressDto);
    }

    default AddressDto mapJsonNodeToAddressDto(JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(jsonNode, AddressDto.class);
    }
}

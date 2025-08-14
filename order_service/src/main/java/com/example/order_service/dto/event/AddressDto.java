package com.example.order_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddressDto {
    @JsonProperty("street")
    private String street;
    
    @JsonProperty("city")
    private String city;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("zipCode")
    private String zipCode;
    
    @JsonProperty("country")
    private String country;
}
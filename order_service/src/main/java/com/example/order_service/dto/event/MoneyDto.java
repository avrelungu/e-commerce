package com.example.order_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class MoneyDto {
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("currency")
    private String currency = "USD";

    public MoneyDto() {}

    public MoneyDto(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
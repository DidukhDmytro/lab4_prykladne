package com.example.carservice.api.dto;

import java.math.BigDecimal;

public record PriceResponse(BigDecimal basePrice, BigDecimal finalPrice) {
}

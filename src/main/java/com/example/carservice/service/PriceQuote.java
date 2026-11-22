package com.example.carservice.service;

import java.math.BigDecimal;

public record PriceQuote(BigDecimal basePrice, BigDecimal finalPrice) {
}

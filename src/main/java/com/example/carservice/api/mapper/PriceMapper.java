package com.example.carservice.api.mapper;

import com.example.carservice.api.dto.PriceResponse;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PriceMapper {
    public PriceResponse toResponse(BigDecimal basePrice, BigDecimal finalPrice) {
        return new PriceResponse(basePrice, finalPrice);
    }
}

package com.example.carservice.api.dto;

import java.math.BigDecimal;

public record PresentResponse(BigDecimal partsTotal, String giftName) {
}

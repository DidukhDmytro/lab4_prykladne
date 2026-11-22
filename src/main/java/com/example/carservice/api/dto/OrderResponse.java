package com.example.carservice.api.dto;

import com.example.carservice.domain.enumerate.OrderPriority;
import com.example.carservice.domain.enumerate.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String vin,
        OrderStatus status,
        OrderPriority priority,
        UUID mechanicId,
        List<WorkResponse> works,
        BigDecimal finalPrice,
        Instant completedAt) {
}

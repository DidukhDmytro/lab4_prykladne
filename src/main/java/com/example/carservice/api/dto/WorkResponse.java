package com.example.carservice.api.dto;

import com.example.carservice.domain.enumerate.MechanicSpecialization;
import com.example.carservice.domain.enumerate.WorkStatus;
import java.math.BigDecimal;

public record WorkResponse(
        int index,
        String description,
        BigDecimal partsCost,
        BigDecimal laborCost,
        MechanicSpecialization requiredSpecialization,
        WorkStatus status) {
}

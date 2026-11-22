package com.example.carservice.api.dto;

import com.example.carservice.api.ApiMessages;
import com.example.carservice.domain.enumerate.MechanicSpecialization;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AddWorkRequest(
        @NotBlank(message = ApiMessages.DESCRIPTION_REQUIRED)
        @Size(max = ApiMessages.MAX_DESCRIPTION_LENGTH, message = ApiMessages.DESCRIPTION_TOO_LONG)
        String description,

        @NotNull(message = ApiMessages.PARTS_COST_REQUIRED)
        @PositiveOrZero(message = ApiMessages.COST_MUST_BE_POSITIVE_OR_ZERO)
        @Digits(
                integer = ApiMessages.MAX_COST_INTEGER_DIGITS,
                fraction = ApiMessages.MAX_COST_FRACTION_DIGITS,
                message = ApiMessages.COST_DIGITS_INVALID)
        BigDecimal partsCost,

        @NotNull(message = ApiMessages.LABOR_COST_REQUIRED)
        @PositiveOrZero(message = ApiMessages.COST_MUST_BE_POSITIVE_OR_ZERO)
        @Digits(
                integer = ApiMessages.MAX_COST_INTEGER_DIGITS,
                fraction = ApiMessages.MAX_COST_FRACTION_DIGITS,
                message = ApiMessages.COST_DIGITS_INVALID)
        BigDecimal laborCost,

        @NotNull(message = ApiMessages.SPECIALIZATION_REQUIRED)
        MechanicSpecialization requiredSpecialization) {
}

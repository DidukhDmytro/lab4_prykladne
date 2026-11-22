package com.example.carservice.api.dto;

import com.example.carservice.api.ApiMessages;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateCarRequest(
        @NotBlank(message = ApiMessages.VIN_REQUIRED)
        @Size(max = 64, message = ApiMessages.VIN_TOO_LONG)
        String vin,

        @Size(max = 100, message = ApiMessages.MAKE_TOO_LONG)
        String make,

        @Size(max = 100, message = ApiMessages.MODEL_TOO_LONG)
        String model,

        @NotNull(message = ApiMessages.YEAR_REQUIRED)
        @Min(value = ApiMessages.MIN_CAR_YEAR, message = ApiMessages.YEAR_OUT_OF_RANGE)
        @Max(value = ApiMessages.MAX_CAR_YEAR, message = ApiMessages.YEAR_OUT_OF_RANGE)
        Integer year,

        @NotNull(message = ApiMessages.OWNER_ID_REQUIRED)
        UUID ownerId) {
}

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
        @Size(max = ApiMessages.MAX_VIN_LENGTH, message = ApiMessages.VIN_TOO_LONG)
        String vin,

        @Size(max = ApiMessages.MAX_CAR_MAKE_LENGTH, message = ApiMessages.MAKE_TOO_LONG)
        String make,

        @Size(max = ApiMessages.MAX_CAR_MODEL_LENGTH, message = ApiMessages.MODEL_TOO_LONG)
        String model,

        @NotNull(message = ApiMessages.YEAR_REQUIRED)
        @Min(value = ApiMessages.MIN_CAR_YEAR, message = ApiMessages.YEAR_OUT_OF_RANGE)
        @Max(value = ApiMessages.MAX_CAR_YEAR, message = ApiMessages.YEAR_OUT_OF_RANGE)
        Integer year,

        @NotNull(message = ApiMessages.OWNER_ID_REQUIRED)
        UUID ownerId) {
}

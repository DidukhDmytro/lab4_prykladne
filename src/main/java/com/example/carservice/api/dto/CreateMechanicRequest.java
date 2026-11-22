package com.example.carservice.api.dto;

import com.example.carservice.api.ApiMessages;
import com.example.carservice.domain.enumerate.MechanicSpecialization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMechanicRequest(
        @NotBlank(message = ApiMessages.FULL_NAME_REQUIRED)
        @Size(max = 255, message = ApiMessages.FULL_NAME_TOO_LONG)
        String fullName,

        @NotNull(message = ApiMessages.SPECIALIZATION_REQUIRED)
        MechanicSpecialization specialization) {
}

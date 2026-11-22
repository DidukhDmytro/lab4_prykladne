package com.example.carservice.api.dto;

import com.example.carservice.api.ApiMessages;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignMechanicRequest(
        @NotNull(message = ApiMessages.MECHANIC_ID_REQUIRED)
        UUID mechanicId) {
}

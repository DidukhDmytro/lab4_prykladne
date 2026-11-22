package com.example.carservice.api.dto;

import com.example.carservice.api.ApiMessages;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank(message = ApiMessages.VIN_REQUIRED)
        String vin) {
}

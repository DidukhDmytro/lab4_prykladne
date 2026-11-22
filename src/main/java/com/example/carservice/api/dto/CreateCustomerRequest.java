package com.example.carservice.api.dto;

import com.example.carservice.api.ApiMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank(message = ApiMessages.FULL_NAME_REQUIRED)
        @Size(max = 255, message = ApiMessages.FULL_NAME_TOO_LONG)
        String fullName,

        @NotBlank(message = ApiMessages.PHONE_NUMBER_REQUIRED)
        @Size(max = 32, message = ApiMessages.PHONE_NUMBER_TOO_LONG)
        String phoneNumber) {
}

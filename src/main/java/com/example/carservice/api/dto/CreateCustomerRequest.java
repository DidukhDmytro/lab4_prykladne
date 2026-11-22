package com.example.carservice.api.dto;

import com.example.carservice.api.ApiMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank(message = ApiMessages.FULL_NAME_REQUIRED)
        @Size(max = ApiMessages.MAX_FULL_NAME_LENGTH, message = ApiMessages.FULL_NAME_TOO_LONG)
        String fullName,

        @NotBlank(message = ApiMessages.PHONE_NUMBER_REQUIRED)
        @Size(max = ApiMessages.MAX_PHONE_NUMBER_LENGTH, message = ApiMessages.PHONE_NUMBER_TOO_LONG)
        String phoneNumber) {
}

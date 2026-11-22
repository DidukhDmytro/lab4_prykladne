package com.example.carservice.api.dto;

import com.example.carservice.api.ApiMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AddWorksRequest(
        @NotEmpty(message = ApiMessages.WORKS_REQUIRED)
        @Valid
        List<AddWorkRequest> works) {
}

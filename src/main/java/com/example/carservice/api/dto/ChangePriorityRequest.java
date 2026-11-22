package com.example.carservice.api.dto;

import com.example.carservice.api.ApiMessages;
import com.example.carservice.domain.enumerate.OrderPriority;
import jakarta.validation.constraints.NotNull;

public record ChangePriorityRequest(
        @NotNull(message = ApiMessages.ORDER_PRIORITY_REQUIRED)
        OrderPriority priority) {
}

package com.example.carservice.api.dto;

import java.util.UUID;

public record CustomerResponse(UUID id, String fullName, String phoneNumber) {
}

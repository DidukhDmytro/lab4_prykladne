package com.example.carservice.api.dto;

import java.util.UUID;

public record CarResponse(String vin, String make, String model, int year, UUID ownerId) {
}

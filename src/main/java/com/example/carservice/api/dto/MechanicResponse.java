package com.example.carservice.api.dto;

import com.example.carservice.domain.enumerate.MechanicSpecialization;
import java.util.UUID;

public record MechanicResponse(
        UUID id,
        String fullName,
        MechanicSpecialization specialization,
        boolean busy) {
}

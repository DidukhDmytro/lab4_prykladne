package com.example.carservice.api.mapper;

import com.example.carservice.api.dto.CreateMechanicRequest;
import com.example.carservice.api.dto.MechanicResponse;
import com.example.carservice.domain.Mechanic;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MechanicMapper {
    public Mechanic toDomain(CreateMechanicRequest request) {
        return new Mechanic(UUID.randomUUID(), request.fullName(), request.specialization());
    }

    public MechanicResponse toResponse(Mechanic mechanic) {
        return new MechanicResponse(
                mechanic.getId(),
                mechanic.getFullName(),
                mechanic.getSpecialization(),
                mechanic.isBusy());
    }
}

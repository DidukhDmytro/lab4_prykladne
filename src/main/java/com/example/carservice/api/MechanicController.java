package com.example.carservice.api;

import com.example.carservice.api.dto.CreateMechanicRequest;
import com.example.carservice.api.dto.MechanicResponse;
import com.example.carservice.api.mapper.MechanicMapper;
import com.example.carservice.domain.Mechanic;
import com.example.carservice.exception.EntityNotFoundException;
import com.example.carservice.exception.ServiceMessages;
import com.example.carservice.service.RegistrationService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MechanicController {
    private final RegistrationService registrationService;
    private final MechanicMapper mechanicMapper;

    @PostMapping(ApiPaths.MECHANICS)
    public ResponseEntity<MechanicResponse> createMechanic(
            @Valid @RequestBody CreateMechanicRequest request) {
        Mechanic mechanic = registrationService.registerMechanic(
                mechanicMapper.toDomain(request));
        MechanicResponse response = mechanicMapper.toResponse(mechanic);
        URI location = URI.create(ApiPaths.MECHANICS + "/" + mechanic.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping(ApiPaths.MECHANIC_BY_ID)
    public MechanicResponse getMechanic(@PathVariable UUID id) {
        Mechanic mechanic = registrationService.findMechanic(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(ServiceMessages.MECHANIC_NOT_FOUND, id)));
        return mechanicMapper.toResponse(mechanic);
    }
}

package com.example.carservice.api;

import com.example.carservice.api.dto.CarResponse;
import com.example.carservice.api.dto.CreateCarRequest;
import com.example.carservice.api.mapper.CarMapper;
import com.example.carservice.domain.Car;
import com.example.carservice.domain.Customer;
import com.example.carservice.exception.EntityNotFoundException;
import com.example.carservice.exception.ServiceMessages;
import com.example.carservice.service.RegistrationService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CarController {
    private final RegistrationService registrationService;
    private final CarMapper carMapper;

    @PostMapping(ApiPaths.CARS)
    public ResponseEntity<CarResponse> createCar(@Valid @RequestBody CreateCarRequest request) {
        Customer owner = registrationService.findCustomer(request.ownerId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(ServiceMessages.CUSTOMER_NOT_FOUND, request.ownerId())));
        Car car = registrationService.registerCar(carMapper.toDomain(request, owner));
        CarResponse response = carMapper.toResponse(car);
        URI location = UriComponentsBuilder.fromPath(ApiPaths.CARS)
                .pathSegment(car.getVin())
                .build()
                .encode()
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping(ApiPaths.CAR_BY_VIN)
    public CarResponse getCar(@PathVariable String vin) {
        Car car = registrationService.findCar(vin)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(ServiceMessages.CAR_NOT_FOUND, vin)));
        return carMapper.toResponse(car);
    }
}

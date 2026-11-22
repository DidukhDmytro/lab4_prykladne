package com.example.carservice.api.mapper;

import com.example.carservice.api.dto.CarResponse;
import com.example.carservice.api.dto.CreateCarRequest;
import com.example.carservice.domain.Car;
import com.example.carservice.domain.Customer;
import org.springframework.stereotype.Component;

@Component
public class CarMapper {
    public Car toDomain(CreateCarRequest request, Customer owner) {
        return new Car(
                request.vin(),
                request.make(),
                request.model(),
                request.year(),
                owner);
    }

    public CarResponse toResponse(Car car) {
        return new CarResponse(
                car.getVin(),
                car.getMake(),
                car.getModel(),
                car.getYear(),
                car.getOwner().getId());
    }
}

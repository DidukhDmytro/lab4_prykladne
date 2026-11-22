package com.example.carservice.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.carservice.api.dto.OrderResponse;
import com.example.carservice.api.dto.PageResponse;
import com.example.carservice.domain.Car;
import com.example.carservice.domain.Customer;
import com.example.carservice.domain.Gift;
import com.example.carservice.domain.Mechanic;
import com.example.carservice.domain.Present;
import com.example.carservice.domain.RepairOrder;
import com.example.carservice.domain.enumerate.MechanicSpecialization;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ApiMapperTest {
    @Test
    void mapsCustomersCarsAndMechanics() {
        Customer customer = new Customer(UUID.randomUUID(), "Customer", "555");
        Car car = new Car("VIN", "Make", "Model", 2020, customer);
        Mechanic mechanic = new Mechanic(UUID.randomUUID(), "Mechanic", MechanicSpecialization.GENERAL);

        assertEquals(customer.getId(), new CustomerMapper().toResponse(customer).id());
        assertEquals(customer.getId(), new CarMapper().toResponse(car).ownerId());
        assertEquals(mechanic.getId(), new MechanicMapper().toResponse(mechanic).id());
    }

    @Test
    void mapsOrderAndPresentWithoutLazyIds() {
        Customer customer = new Customer(UUID.randomUUID(), "Customer", "555");
        Car car = new Car("VIN", "Make", "Model", 2020, customer);
        RepairOrder order = new RepairOrder(UUID.randomUUID(), car);

        OrderResponse response = new OrderMapper().toResponse(order);
        assertEquals(order.getId(), response.id());
        assertEquals("VIN", response.vin());
        assertNull(response.mechanicId());
        assertEquals(BigDecimal.ZERO, new PresentMapper()
                .toResponse(new Present(BigDecimal.ZERO, new Gift("Gift", BigDecimal.ZERO)))
                .partsTotal());
    }

    @Test
    void mapsSpringPage() {
        Page<String> page = new PageImpl<>(
                java.util.List.of("one"),
                PageRequest.of(0, 20),
                21);
        PageResponse<String> response = new PageMapper()
                .toResponse(page, value -> value);

        assertEquals(1, response.content().size());
        assertEquals(0, response.page());
        assertEquals(20, response.size());
        assertEquals(21, response.totalElements());
        assertEquals(2, response.totalPages());
    }
}

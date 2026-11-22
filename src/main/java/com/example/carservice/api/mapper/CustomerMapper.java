package com.example.carservice.api.mapper;

import com.example.carservice.api.dto.CreateCustomerRequest;
import com.example.carservice.api.dto.CustomerResponse;
import com.example.carservice.domain.Customer;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public Customer toDomain(CreateCustomerRequest request) {
        return new Customer(UUID.randomUUID(), request.fullName(), request.phoneNumber());
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getPhoneNumber());
    }
}

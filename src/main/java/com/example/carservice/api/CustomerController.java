package com.example.carservice.api;

import com.example.carservice.api.dto.CreateCustomerRequest;
import com.example.carservice.api.dto.CustomerResponse;
import com.example.carservice.api.mapper.CustomerMapper;
import com.example.carservice.domain.Customer;
import com.example.carservice.exception.EntityNotFoundException;
import com.example.carservice.exception.ServiceMessages;
import com.example.carservice.service.RegistrationService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
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
public class CustomerController {
    private final RegistrationService registrationService;
    private final CustomerMapper customerMapper;

    @PostMapping(ApiPaths.CUSTOMERS)
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = registrationService.registerCustomer(
                customerMapper.toDomain(request));
        CustomerResponse response = customerMapper.toResponse(customer);
        URI location = UriComponentsBuilder.fromPath(ApiPaths.CUSTOMERS)
                .pathSegment(customer.getId().toString())
                .build()
                .encode()
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping(ApiPaths.CUSTOMER_BY_ID)
    public CustomerResponse getCustomer(@PathVariable UUID id) {
        Customer customer = registrationService.findCustomer(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(ServiceMessages.CUSTOMER_NOT_FOUND, id)));
        return customerMapper.toResponse(customer);
    }
}

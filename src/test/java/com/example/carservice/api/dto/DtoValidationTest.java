package com.example.carservice.api.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.carservice.api.ApiMessages;
import com.example.carservice.domain.enumerate.MechanicSpecialization;
import com.example.carservice.domain.enumerate.OrderPriority;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DtoValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @ParameterizedTest
    @MethodSource("invalidCustomers")
    void rejectsInvalidCustomers(CreateCustomerRequest request) {
        assertFalse(validator.validate(request).isEmpty());
    }

    static List<Arguments> invalidCustomers() {
        return List.of(
                Arguments.of(new CreateCustomerRequest(null, "555")),
                Arguments.of(new CreateCustomerRequest(" ", "555")),
                Arguments.of(new CreateCustomerRequest("A".repeat(256), "555")),
                Arguments.of(new CreateCustomerRequest("Name", null)),
                Arguments.of(new CreateCustomerRequest("Name", "1".repeat(33))));
    }

    @ParameterizedTest
    @MethodSource("invalidCars")
    void rejectsInvalidCars(CreateCarRequest request) {
        assertFalse(validator.validate(request).isEmpty());
    }

    static List<Arguments> invalidCars() {
        UUID ownerId = UUID.randomUUID();
        return List.of(
                Arguments.of(new CreateCarRequest("", null, null, 2020, ownerId)),
                Arguments.of(new CreateCarRequest("VIN", null, null, null, ownerId)),
                Arguments.of(new CreateCarRequest("VIN", null, null, 1885, ownerId)),
                Arguments.of(new CreateCarRequest("VIN", null, null, 2101, ownerId)),
                Arguments.of(new CreateCarRequest("VIN", null, null, 2020, null)),
                Arguments.of(new CreateCarRequest("V".repeat(65), "M".repeat(101), "M".repeat(101), 2020, ownerId)));
    }

    @Test
    void rejectsInvalidMechanicAndPriority() {
        assertFalse(validator.validate(new CreateMechanicRequest("", null)).isEmpty());
        assertFalse(validator.validate(new AssignMechanicRequest(null)).isEmpty());
        assertFalse(validator.validate(new ChangePriorityRequest(null)).isEmpty());
        assertFalse(validator.validate(new CreateOrderRequest("")).isEmpty());
    }

    @ParameterizedTest
    @MethodSource("invalidWorks")
    void rejectsInvalidWorks(AddWorkRequest request) {
        assertFalse(validator.validate(request).isEmpty());
    }

    static List<Arguments> invalidWorks() {
        return List.of(
                Arguments.of(new AddWorkRequest("", BigDecimal.ONE, BigDecimal.ONE, MechanicSpecialization.GENERAL)),
                Arguments.of(new AddWorkRequest("Work", null, BigDecimal.ONE, MechanicSpecialization.GENERAL)),
                Arguments.of(new AddWorkRequest("Work", new BigDecimal("-0.01"), BigDecimal.ONE, MechanicSpecialization.GENERAL)),
                Arguments.of(new AddWorkRequest("Work", BigDecimal.ONE, null, MechanicSpecialization.GENERAL)),
                Arguments.of(new AddWorkRequest("Work", BigDecimal.ONE, new BigDecimal("-0.01"), MechanicSpecialization.GENERAL)),
                Arguments.of(new AddWorkRequest("Work", BigDecimal.ONE, BigDecimal.ONE, null)),
                Arguments.of(new AddWorkRequest("Work", new BigDecimal("12345678901"), BigDecimal.ONE, MechanicSpecialization.GENERAL)),
                Arguments.of(new AddWorkRequest("Work", new BigDecimal("1.001"), BigDecimal.ONE, MechanicSpecialization.GENERAL)));
    }

    @Test
    void rejectsEmptyBatchAndAcceptsValidBatch() {
        assertFalse(validator.validate(new AddWorksRequest(List.of())).isEmpty());

        AddWorkRequest work = new AddWorkRequest(
                "Inspection",
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                MechanicSpecialization.GENERAL);
        Set<ConstraintViolation<AddWorksRequest>> violations =
                validator.validate(new AddWorksRequest(List.of(work)));
        assertTrue(violations.isEmpty());
    }

    @Test
    void constantsAreSane() {
        assertTrue(ApiMessages.MIN_CAR_YEAR < ApiMessages.MAX_CAR_YEAR);
        assertTrue(validator.validate(new ChangePriorityRequest(OrderPriority.NORMAL)).isEmpty());
    }
}

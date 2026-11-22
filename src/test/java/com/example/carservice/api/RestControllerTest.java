package com.example.carservice.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carservice.api.error.ApiExceptionHandler;
import com.example.carservice.api.mapper.CarMapper;
import com.example.carservice.api.mapper.CustomerMapper;
import com.example.carservice.api.mapper.MechanicMapper;
import com.example.carservice.api.mapper.OrderMapper;
import com.example.carservice.api.mapper.PageMapper;
import com.example.carservice.api.mapper.PresentMapper;
import com.example.carservice.api.mapper.PriceMapper;
import com.example.carservice.domain.Car;
import com.example.carservice.domain.Customer;
import com.example.carservice.domain.Mechanic;
import com.example.carservice.domain.RepairOrder;
import com.example.carservice.domain.enumerate.MechanicSpecialization;
import com.example.carservice.exception.EntityAlreadyExistsException;
import com.example.carservice.exception.EntityNotFoundException;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.exception.InvalidStateTransitionException;
import com.example.carservice.service.RegistrationService;
import com.example.carservice.service.RepairOrderService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RestControllerTest {
    private RegistrationService registrationService;
    private RepairOrderService repairOrderService;
    private MockMvc customerMvc;
    private MockMvc carMvc;
    private MockMvc mechanicMvc;
    private MockMvc orderMvc;

    @BeforeEach
    void setUp() {
        registrationService = Mockito.mock(RegistrationService.class);
        repairOrderService = Mockito.mock(RepairOrderService.class);
        ApiExceptionHandler advice = new ApiExceptionHandler();

        customerMvc = MockMvcBuilders.standaloneSetup(new CustomerController(
                        registrationService,
                        new CustomerMapper()))
                .setControllerAdvice(advice)
                .build();
        carMvc = MockMvcBuilders.standaloneSetup(new CarController(
                        registrationService,
                        new CarMapper()))
                .setControllerAdvice(advice)
                .build();
        mechanicMvc = MockMvcBuilders.standaloneSetup(new MechanicController(
                        registrationService,
                        new MechanicMapper()))
                .setControllerAdvice(advice)
                .build();
        orderMvc = MockMvcBuilders.standaloneSetup(new OrderController(
                        repairOrderService,
                        new OrderMapper(),
                        new PriceMapper(),
                        new PresentMapper(),
                        new PageMapper()))
                .setControllerAdvice(advice)
                .build();
    }

    @Test
    void customerCreateReturns201LocationAndGetReturns404() throws Exception {
        UUID id = UUID.fromString("00000000-0000-4000-8000-000000000101");
        Customer customer = new Customer(id, "Alex", "555");
        when(registrationService.registerCustomer(any(Customer.class))).thenReturn(customer);

        customerMvc.perform(post(ApiPaths.CUSTOMERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Alex\",\"phoneNumber\":\"555\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", ApiPaths.CUSTOMERS + "/" + id));

        verify(registrationService).registerCustomer(any(Customer.class));

        when(registrationService.findCustomer(id)).thenReturn(Optional.empty());
        customerMvc.perform(get(ApiPaths.CUSTOMER_BY_ID, id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value(ApiMessages.NOT_FOUND_TITLE));
    }

    @Test
    void invalidCustomerBodyReturns400WithErrors() throws Exception {
        customerMvc.perform(post(ApiPaths.CUSTOMERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"\",\"phoneNumber\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").exists());
    }

    @Test
    void carCreateReturns201AndVerifyOwnerLookup() throws Exception {
        UUID ownerId = UUID.randomUUID();
        Customer owner = new Customer(ownerId, "Owner", "555");
        Car car = new Car("VIN-REST", "Make", "Model", 2020, owner);
        when(registrationService.findCustomer(ownerId)).thenReturn(Optional.of(owner));
        when(registrationService.registerCar(any(Car.class))).thenReturn(car);

        carMvc.perform(post(ApiPaths.CARS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vin\":\"VIN-REST\",\"make\":\"Make\",\"model\":\"Model\",\"year\":2020,\"ownerId\":\""
                                + ownerId + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", ApiPaths.CARS + "/VIN-REST"))
                .andExpect(jsonPath("$.ownerId").value(ownerId.toString()));

        verify(registrationService).findCustomer(ownerId);
        verify(registrationService).registerCar(any(Car.class));
    }

    @Test
    void mechanicCreateReturns201AndGetReturns200() throws Exception {
        UUID mechanicId = UUID.randomUUID();
        Mechanic mechanic = new Mechanic(mechanicId, "Mechanic", MechanicSpecialization.GENERAL);
        when(registrationService.registerMechanic(any(Mechanic.class))).thenReturn(mechanic);
        when(registrationService.findMechanic(mechanicId)).thenReturn(Optional.of(mechanic));

        mechanicMvc.perform(post(ApiPaths.MECHANICS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Mechanic\",\"specialization\":\"GENERAL\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", ApiPaths.MECHANICS + "/" + mechanicId));

        mechanicMvc.perform(get(ApiPaths.MECHANIC_BY_ID, mechanicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.busy").value(false));
    }

    @Test
    void orderCreateAndStatusActionReturn200Or201() throws Exception {
        UUID orderId = UUID.randomUUID();
        Customer customer = new Customer(UUID.randomUUID(), "Owner", "555");
        Car car = new Car("VIN-REST", "Make", "Model", 2020, customer);
        RepairOrder order = new RepairOrder(orderId, car);
        when(repairOrderService.createOrder(any(UUID.class), eq("VIN-REST"))).thenReturn(order);
        when(repairOrderService.diagnose(orderId)).thenReturn(order);

        orderMvc.perform(post(ApiPaths.ORDERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vin\":\"VIN-REST\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        orderMvc.perform(post(ApiPaths.DIAGNOSE_ORDER, orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()));

        verify(repairOrderService).createOrder(any(UUID.class), eq("VIN-REST"));
        verify(repairOrderService).diagnose(orderId);
    }

    @Test
    void orderNotFoundAndConflictAreMapped() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(repairOrderService.findOrder(orderId)).thenReturn(Optional.empty());
        orderMvc.perform(get(ApiPaths.ORDER_BY_ID, orderId))
                .andExpect(status().isNotFound());

        when(repairOrderService.approve(orderId))
                .thenThrow(new InvalidStateTransitionException("Invalid transition"));
        orderMvc.perform(post(ApiPaths.APPROVE_ORDER, orderId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value(ApiMessages.CONFLICT_TITLE));
    }

    @Test
    void duplicateCreateIs409() throws Exception {
        UUID ownerId = UUID.randomUUID();
        Customer owner = new Customer(ownerId, "Owner", "555");
        when(registrationService.findCustomer(ownerId)).thenReturn(Optional.of(owner));
        when(registrationService.registerCar(any(Car.class)))
                .thenThrow(new EntityAlreadyExistsException("Duplicate VIN"));

        carMvc.perform(post(ApiPaths.CARS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vin\":\"DUP\",\"make\":\"Make\",\"model\":\"Model\",\"year\":2020,\"ownerId\":\""
                                + ownerId + "\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Duplicate VIN"));
    }
}

package com.example.carservice.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.carservice.domain.Car;
import com.example.carservice.domain.Customer;
import com.example.carservice.domain.Mechanic;
import com.example.carservice.domain.RepairOrder;
import com.example.carservice.domain.ServiceWork;
import com.example.carservice.domain.enumerate.MechanicSpecialization;
import com.example.carservice.domain.enumerate.OrderStatus;
import com.example.carservice.repository.CarRepository;
import com.example.carservice.repository.CustomerRepository;
import com.example.carservice.repository.MechanicRepository;
import com.example.carservice.repository.RepairOrderRepository;
import com.example.carservice.service.RegistrationService;
import com.example.carservice.service.RepairOrderService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class RestApiIT extends IntegrationTest {
    private static final Instant FIXED_INSTANT = Instant.parse("2026-10-29T12:00:00Z");
    private static final BigDecimal FIRST_PARTS = new BigDecimal("200.00");
    private static final BigDecimal FIRST_LABOR = new BigDecimal("85.00");
    private static final BigDecimal SECOND_PARTS = new BigDecimal("60.00");
    private static final BigDecimal SECOND_LABOR = new BigDecimal("45.00");
    private static final int CAR_YEAR = 2020;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private MechanicRepository mechanicRepository;

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    @Test
    void completeOperationPersistsFixedCompletionAndPrice() throws Exception {
        Fixture fixture = registerFixture("COMPLETE-VIN");
        Mechanic generalMechanic = registrationService.registerMechanic(
                new Mechanic(UUID.randomUUID(), "General mechanic", MechanicSpecialization.GENERAL));
        repairOrderService.createOrder(fixture.orderId(), fixture.vin());
        repairOrderService.addWorks(fixture.orderId(), java.util.List.of(
                engineWork(),
                brakesWork()));
        repairOrderService.diagnose(fixture.orderId());
        repairOrderService.approve(fixture.orderId());
        repairOrderService.assignMechanic(fixture.orderId(), generalMechanic.getId());
        repairOrderService.startProgress(fixture.orderId());
        repairOrderService.completeWork(fixture.orderId(), 0);
        repairOrderService.completeWork(fixture.orderId(), 1);

        mockMvc.perform(post("/api/orders/{id}/complete", fixture.orderId()))
                .andExpect(status().isOk());

        RepairOrder order = repairOrderRepository.findById(fixture.orderId()).orElseThrow();
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(FIXED_INSTANT, order.getCompletedAt());
        assertEquals(0, new BigDecimal("390.00").compareTo(order.getFinalPrice()));
        Integer workCount = jdbcTemplate.queryForObject(
                "select count(*) from service_works where order_id = ?",
                Integer.class,
                fixture.orderId());
        assertEquals(2, workCount);
    }

    @Test
    void incompatibleBatchIsConflictAndSavesZeroWorks() throws Exception {
        Fixture fixture = registerFixture("BATCH-VIN");
        repairOrderService.createOrder(fixture.orderId(), fixture.vin());
        repairOrderService.addWork(fixture.orderId(),
                new ServiceWork("Initial engine work", BigDecimal.ZERO, BigDecimal.ONE, MechanicSpecialization.ENGINE));
        repairOrderService.diagnose(fixture.orderId());
        repairOrderService.approve(fixture.orderId());
        repairOrderService.assignMechanic(fixture.orderId(), fixture.mechanicId());

        mockMvc.perform(post("/api/orders/{id}/works/batch", fixture.orderId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "works":[
                                    {"description":"Compatible","partsCost":10,"laborCost":10,"requiredSpecialization":"ENGINE"},
                                    {"description":"Incompatible","partsCost":10,"laborCost":10,"requiredSpecialization":"BRAKES"}
                                  ]
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));

        assertEquals(1, jdbcTemplate.queryForObject(
                "select count(*) from service_works where order_id = ?",
                Integer.class,
                fixture.orderId()));
        assertEquals(1, repairOrderRepository.findById(fixture.orderId()).orElseThrow().getWorks().size());
    }

    @Test
    void rejectedLifecycleOperationsLeaveDatabaseUnchanged() throws Exception {
        Customer customer = registerCustomer("Rejected");
        Car car = registerCar("REJECT-VIN", customer);
        UUID emptyOrderId = UUID.randomUUID();
        repairOrderService.createOrder(emptyOrderId, car.getVin());

        mockMvc.perform(post("/api/orders/{id}/approve", emptyOrderId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
        assertEquals(OrderStatus.CREATED,
                repairOrderRepository.findById(emptyOrderId).orElseThrow().getStatus());

        UUID noMechanicOrderId = UUID.randomUUID();
        repairOrderService.createOrder(noMechanicOrderId, car.getVin());
        repairOrderService.addWork(noMechanicOrderId,
                new ServiceWork("General", BigDecimal.ZERO, BigDecimal.ONE, MechanicSpecialization.GENERAL));
        repairOrderService.diagnose(noMechanicOrderId);
        repairOrderService.approve(noMechanicOrderId);
        mockMvc.perform(post("/api/orders/{id}/start", noMechanicOrderId))
                .andExpect(status().isConflict());
        assertEquals(OrderStatus.APPROVED,
                repairOrderRepository.findById(noMechanicOrderId).orElseThrow().getStatus());

        Mechanic mechanic = registrationService.registerMechanic(
                new Mechanic(UUID.randomUUID(), "Busy mechanic", MechanicSpecialization.GENERAL));
        UUID firstOrderId = UUID.randomUUID();
        repairOrderService.createOrder(firstOrderId, car.getVin());
        repairOrderService.addWork(firstOrderId,
                new ServiceWork("General", BigDecimal.ZERO, BigDecimal.ONE, MechanicSpecialization.GENERAL));
        repairOrderService.diagnose(firstOrderId);
        repairOrderService.approve(firstOrderId);
        repairOrderService.assignMechanic(firstOrderId, mechanic.getId());

        UUID secondOrderId = UUID.randomUUID();
        repairOrderService.createOrder(secondOrderId, car.getVin());
        repairOrderService.addWork(secondOrderId,
                new ServiceWork("General", BigDecimal.ZERO, BigDecimal.ONE, MechanicSpecialization.GENERAL));
        mockMvc.perform(put("/api/orders/{id}/mechanic", secondOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mechanicId\":\"" + mechanic.getId() + "\"}"))
                .andExpect(status().isConflict());

        assertTrue(mechanicRepository.findById(mechanic.getId()).orElseThrow().isBusy());
        assertEquals(OrderStatus.APPROVED,
                repairOrderRepository.findById(firstOrderId).orElseThrow().getStatus());
        assertEquals(OrderStatus.CREATED,
                repairOrderRepository.findById(secondOrderId).orElseThrow().getStatus());

        UUID completedOrderId = UUID.randomUUID();
        Mechanic completedMechanic = registrationService.registerMechanic(
                new Mechanic(UUID.randomUUID(), "Completed mechanic", MechanicSpecialization.GENERAL));
        repairOrderService.createOrder(completedOrderId, car.getVin());
        repairOrderService.addWork(completedOrderId,
                new ServiceWork("General", BigDecimal.ZERO, BigDecimal.ONE, MechanicSpecialization.GENERAL));
        repairOrderService.diagnose(completedOrderId);
        repairOrderService.approve(completedOrderId);
        repairOrderService.assignMechanic(completedOrderId, completedMechanic.getId());
        repairOrderService.startProgress(completedOrderId);
        repairOrderService.completeWork(completedOrderId, 0);
        repairOrderService.complete(completedOrderId);

        mockMvc.perform(put("/api/orders/{id}/priority", completedOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priority\":\"URGENT\"}"))
                .andExpect(status().isConflict());
        assertEquals(com.example.carservice.domain.enumerate.OrderPriority.NORMAL,
                repairOrderRepository.findById(completedOrderId).orElseThrow().getPriority());
    }

    @Test
    void duplicateVinReturns409AndLeavesOneCarRow() throws Exception {
        Customer customer = registerCustomer("Duplicate");
        registerCar("DUPLICATE-VIN", customer);

        mockMvc.perform(post("/api/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vin":"DUPLICATE-VIN","make":"Make","model":"Model","year":2020,"ownerId":"%s"}
                                """.formatted(customer.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").exists());

        assertEquals(1, jdbcTemplate.queryForObject(
                "select count(*) from cars where vin = 'DUPLICATE-VIN'",
                Integer.class));
    }

    @Test
    void carGetWorksWithOpenInViewFalseAndValidationAndUnknownPaths() throws Exception {
        Customer customer = registerCustomer("Open View");
        registerCar("OPEN-VIEW-VIN", customer);

        mockMvc.perform(get("/api/cars/{vin}", "OPEN-VIEW-VIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value(customer.getId().toString()));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"\",\"phoneNumber\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").exists());
        assertEquals(1, customerRepository.count());

        mockMvc.perform(get("/api/orders/{id}",
                        "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").exists());
    }

    @Test
    void paginationWorksWithFilterSortClampingAndFetchGraph() throws Exception {
        Customer customer = registerCustomer("Pagination");
        Car car = registerCar("PAGINATION-VIN", customer);
        for (int index = 0; index < 55; index++) {
            UUID orderId = UUID.randomUUID();
            repairOrderService.createOrder(orderId, car.getVin());
            repairOrderService.addWork(orderId,
                    new ServiceWork("Work " + index, BigDecimal.ZERO, BigDecimal.ONE,
                            MechanicSpecialization.GENERAL));
        }

        mockMvc.perform(get("/api/orders")
                        .param("status", "CREATED")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "priority,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].works").isArray());

        mockMvc.perform(get("/api/orders")
                        .param("status", "CREATED")
                        .param("page", "0")
                        .param("size", "50")
                        .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.content.length()").value(50));

        mockMvc.perform(get("/api/orders")
                        .param("status", "CREATED")
                        .param("page", "0")
                        .param("size", "51")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.content.length()").value(50));

        mockMvc.perform(get("/api/orders")
                        .param("status", "CREATED")
                        .param("page", "0")
                        .param("size", "0")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.content.length()").value(20));

        mockMvc.perform(get("/api/orders")
                        .param("status", "CREATED")
                        .param("page", "100")
                        .param("size", "20")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        mockMvc.perform(get("/api/orders")
                        .param("sort", "works,asc"))
                .andExpect(status().isBadRequest());
    }

    private Customer registerCustomer(String name) {
        return registrationService.registerCustomer(
                new Customer(UUID.randomUUID(), name, "555-0100"));
    }

    private Car registerCar(String vin, Customer customer) {
        return registrationService.registerCar(
                new Car(vin, "Make", "Model", CAR_YEAR, customer));
    }

    private Fixture registerFixture(String vin) {
        Customer customer = registerCustomer("Fixture");
        Car car = registerCar(vin, customer);
        Mechanic mechanic = registrationService.registerMechanic(
                new Mechanic(UUID.randomUUID(), "Engine mechanic", MechanicSpecialization.ENGINE));
        UUID orderId = UUID.randomUUID();
        return new Fixture(vin, mechanic.getId(), orderId);
    }

    private ServiceWork engineWork() {
        return new ServiceWork("Engine", FIRST_PARTS, FIRST_LABOR, MechanicSpecialization.ENGINE);
    }

    private ServiceWork brakesWork() {
        return new ServiceWork("Brakes", SECOND_PARTS, SECOND_LABOR, MechanicSpecialization.BRAKES);
    }

    private record Fixture(String vin, UUID mechanicId, UUID orderId) {
    }
}

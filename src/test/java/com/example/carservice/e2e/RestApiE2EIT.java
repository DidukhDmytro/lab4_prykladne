package com.example.carservice.e2e;

import com.example.carservice.api.dto.OrderResponse;
import com.example.carservice.support.PostgreSqlTestContainerConfiguration;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("test")
@Import(PostgreSqlTestContainerConfiguration.class)
class RestApiE2EIT {
    /**
     * E2E tests are not transactional. The HTTP server handles requests on its own request thread
     * with separate service transactions, so a test-managed transaction would neither observe nor
     * roll back the server-side database changes reliably.
     */
    @Autowired
    private RestTestClient restTestClient;

    @Test
    void fullLifecycleThroughHttp() {
        String suffix = UUID.randomUUID().toString();
        String vin = "E2E-" + suffix;
        UUID customerId = createCustomer(suffix);
        createCar(vin, customerId);
        UUID mechanicId = createMechanic("Mechanic-" + suffix);

        OrderResponse normal = createOrder(vin);
        addSingleWork(normal.id());
        addBatchWork(normal.id());
        diagnose(normal.id());
        approve(normal.id());
        assignMechanic(normal.id(), mechanicId);
        start(normal.id());
        completeWork(normal.id(), 0);
        completeWork(normal.id(), 1);
        normal = complete(normal.id());

        assertEqualsBigDecimal("390.00",
                restTestClient.get().uri("/api/orders/{id}/price", normal.id())
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(Map.class)
                        .returnResult().getResponseBody().get("finalPrice"));

        restTestClient.get().uri("/api/orders/{id}", normal.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponse.class)
                .value(order -> {
                    if (!"COMPLETED".equals(order.status().name())) {
                        throw new AssertionError("Expected completed status");
                    }
                    if (order.completedAt() == null) {
                        throw new AssertionError("Expected completedAt");
                    }
                });

        OrderResponse urgent = createOrder(vin);
        addUrgentWorks(urgent.id());
        restTestClient.put().uri("/api/orders/{id}/priority", urgent.id())
                .body(Map.of("priority", "URGENT"))
                .exchange()
                .expectStatus().isOk();
        assertEqualsBigDecimal("416.00",
                restTestClient.get().uri("/api/orders/{id}/price", urgent.id())
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody(Map.class)
                        .returnResult().getResponseBody().get("finalPrice"));
    }

    @Test
    void busyMechanicAndRejectedTransitionsReturnProblemDetails() {
        String suffix = UUID.randomUUID().toString();
        String vin = "E2E-" + suffix;
        UUID customerId = createCustomer(suffix);
        createCar(vin, customerId);
        UUID mechanicId = createMechanic("Busy-" + suffix);

        OrderResponse first = createOrder(vin);
        addBatchWork(first.id());
        diagnose(first.id());
        approve(first.id());
        assignMechanic(first.id(), mechanicId);
        start(first.id());

        OrderResponse second = createOrder(vin);
        addBatchWork(second.id());
        Map<String, Object> body = problemDetail(
                restTestClient.put().uri("/api/orders/{id}/mechanic", second.id())
                        .body(Map.of("mechanicId", mechanicId))
                        .exchange()
                        .expectStatus().isEqualTo(409));

        assertProblemDetail(body);

        restTestClient.get().uri("/api/orders/{id}", first.id())
                .exchange().expectStatus().isOk()
                .expectBody(OrderResponse.class)
                .value(order -> {
                    if (!mechanicId.equals(order.mechanicId())
                            || !"IN_PROGRESS".equals(order.status().name())) {
                        throw new AssertionError("First order changed unexpectedly");
                    }
                });
        restTestClient.get().uri("/api/orders/{id}", second.id())
                .exchange().expectStatus().isOk()
                .expectBody(OrderResponse.class)
                .value(order -> {
                    if (order.mechanicId() != null) {
                        throw new AssertionError("Second order should remain unassigned");
                    }
                });

        OrderResponse empty = createOrder(vin);
        problemDetail(restTestClient.post().uri("/api/orders/{id}/approve", empty.id())
                .exchange().expectStatus().isEqualTo(409));

        UUID noMechanicOrderId = createOrder(vin).id();
        addSingleWork(noMechanicOrderId);
        diagnose(noMechanicOrderId);
        approve(noMechanicOrderId);
        problemDetail(restTestClient.post().uri("/api/orders/{id}/start", noMechanicOrderId)
                .exchange().expectStatus().isEqualTo(409));

        restTestClient.put().uri("/api/orders/{id}/diagnose", empty.id())
                .exchange().expectStatus().is4xxClientError();
        restTestClient.patch().uri("/api/orders/{id}/approve", empty.id())
                .exchange().expectStatus().is4xxClientError();
    }

    @Test
    void validationDuplicateUnknownAndCompletedModification() {
        String suffix = UUID.randomUUID().toString();
        String vin = "E2E-" + suffix;
        UUID customerId = createCustomer(suffix);
        createCar(vin, customerId);

        restTestClient.post().uri("/api/customers")
                .body(Map.of("fullName", "", "phoneNumber", ""))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0].field").exists();

        restTestClient.post().uri("/api/cars")
                .body(Map.of(
                        "vin", vin,
                        "make", "Duplicate",
                        "model", "Reject",
                        "year", 2020,
                        "ownerId", customerId))
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.title").isEqualTo("Conflict")
                .jsonPath("$.detail").exists();

        restTestClient.get().uri("/api/orders/{id}",
                        "00000000-0000-0000-0000-000000000000")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.title").isEqualTo("Not Found");

        UUID mechanicId = createMechanic("Complete-" + suffix);
        OrderResponse completed = createOrder(vin);
        addBatchWork(completed.id());
        diagnose(completed.id());
        approve(completed.id());
        assignMechanic(completed.id(), mechanicId);
        start(completed.id());
        completeWork(completed.id(), 0);
        complete(completed.id());

        restTestClient.put().uri("/api/orders/{id}/priority", completed.id())
                .body(Map.of("priority", "URGENT"))
                .exchange()
                .expectStatus().isEqualTo(409);

        restTestClient.post().uri("/api/orders/{id}/diagnose", completed.id())
                .exchange()
                .expectStatus().isEqualTo(409);

        assertProblemSafe(restTestClient.get().uri("/api/orders/{id}",
                        "00000000-0000-0000-0000-000000000000")
                .exchange().expectStatus().isNotFound());
    }

    @Test
    void paginationBoundariesAndFilter() {
        String suffix = UUID.randomUUID().toString();
        String vin = "E2E-" + suffix;
        UUID customerId = createCustomer(suffix);
        createCar(vin, customerId);
        UUID[] ownIds = new UUID[55];
        for (int index = 0; index < ownIds.length; index++) {
            ownIds[index] = createOrder(vin).id();
        }

        restTestClient.get().uri("/api/orders?status=CREATED&page=0&size=1&sort=id,asc")
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.size").isEqualTo(1)
                .jsonPath("$.content").isArray();

        restTestClient.get().uri("/api/orders?status=CREATED&page=0&size=50&sort=id,asc")
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.size").isEqualTo(50);

        restTestClient.get().uri("/api/orders?status=CREATED&page=0&size=51&sort=id,asc")
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.size").isEqualTo(50);

        restTestClient.get().uri("/api/orders?status=CREATED&page=0&size=0&sort=id,asc")
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.size").isEqualTo(20);

        restTestClient.get().uri("/api/orders?status=CREATED&page=100&size=20&sort=id,asc")
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray();

        restTestClient.get().uri("/api/orders?sort=works,asc")
                .exchange().expectStatus().isBadRequest();

        restTestClient.get().uri("/api/orders?status=CREATED&page=0&size=20&sort=id,asc")
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[*].id")
                .value(ids -> {
                    if (!(ids instanceof List<?> list)) {
                        throw new AssertionError("Expected ID list");
                    }
                    for (Object id : list) {
                        if (!containsOwnId(ownIds, id.toString())) {
                            continue;
                        }
                        return;
                    }
                });
    }

    private UUID createCustomer(String suffix) {
        Map<String, Object> body = Map.of(
                "fullName", "E2E Customer " + suffix,
                "phoneNumber", "555-" + suffix.substring(0, 8));
        return UUID.fromString(restTestClient.post().uri("/api/customers")
                .body(body)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult().getResponseBody().get("id").toString());
    }

    private void createCar(String vin, UUID ownerId) {
        restTestClient.post().uri("/api/cars")
                .body(Map.of(
                        "vin", vin,
                        "make", "E2E",
                        "model", "Test",
                        "year", 2020,
                        "ownerId", ownerId))
                .exchange()
                .expectStatus().isCreated();
    }

    private UUID createMechanic(String name) {
        return UUID.fromString(restTestClient.post().uri("/api/mechanics")
                .body(Map.of("fullName", name, "specialization", "GENERAL"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .returnResult().getResponseBody().get("id").toString());
    }

    private OrderResponse createOrder(String vin) {
        return restTestClient.post().uri("/api/orders")
                .body(Map.of("vin", vin))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponse.class)
                .returnResult().getResponseBody();
    }

    private void addSingleWork(UUID orderId) {
        restTestClient.post().uri("/api/orders/{id}/works", orderId)
                .body(Map.of(
                        "description", "Engine",
                        "partsCost", 200.00,
                        "laborCost", 85.00,
                        "requiredSpecialization", "ENGINE"))
                .exchange().expectStatus().isCreated();
    }

    private void addBatchWork(UUID orderId) {
        restTestClient.post().uri("/api/orders/{id}/works/batch", orderId)
                .body(Map.of("works", List.of(
                        Map.of(
                                "description", "Brakes",
                                "partsCost", 60.00,
                                "laborCost", 45.00,
                                "requiredSpecialization", "BRAKES"))))
                .exchange().expectStatus().isCreated();
    }

    private void addUrgentWorks(UUID orderId) {
        restTestClient.post().uri("/api/orders/{id}/works/batch", orderId)
                .body(Map.of("works", List.of(
                        Map.of(
                                "description", "Engine",
                                "partsCost", 200.00,
                                "laborCost", 85.00,
                                "requiredSpecialization", "ENGINE"),
                        Map.of(
                                "description", "Brakes",
                                "partsCost", 60.00,
                                "laborCost", 45.00,
                                "requiredSpecialization", "BRAKES"))))
                .exchange().expectStatus().isCreated();
    }

    private void diagnose(UUID orderId) {
        restTestClient.post().uri("/api/orders/{id}/diagnose", orderId)
                .exchange().expectStatus().isOk();
    }

    private void approve(UUID orderId) {
        restTestClient.post().uri("/api/orders/{id}/approve", orderId)
                .exchange().expectStatus().isOk();
    }

    private void assignMechanic(UUID orderId, UUID mechanicId) {
        restTestClient.put().uri("/api/orders/{id}/mechanic", orderId)
                .body(Map.of("mechanicId", mechanicId))
                .exchange().expectStatus().isOk();
    }

    private void start(UUID orderId) {
        restTestClient.post().uri("/api/orders/{id}/start", orderId)
                .exchange().expectStatus().isOk();
    }

    private void completeWork(UUID orderId, int index) {
        restTestClient.post().uri("/api/orders/{id}/works/{index}/complete", orderId, index)
                .exchange().expectStatus().isOk();
    }

    private OrderResponse complete(UUID orderId) {
        return restTestClient.post().uri("/api/orders/{id}/complete", orderId)
                .exchange().expectStatus().isOk()
                .expectBody(OrderResponse.class)
                .returnResult().getResponseBody();
    }

    private Map<String, Object> problemDetail(
            RestTestClient.ResponseSpec response) {
        return response.expectBody(Map.class).returnResult().getResponseBody();
    }

    private void assertProblemDetail(Map<String, Object> body) {
        if (!body.containsKey("status")
                || !body.containsKey("title")
                || !body.containsKey("detail")) {
            throw new AssertionError("Missing ProblemDetail fields");
        }
    }

    private void assertProblemSafe(RestTestClient.ResponseSpec response) {
        response.expectBody()
                .consumeWith(result -> {
                    String body = new String(
                            result.getResponseBodyContent(),
                            StandardCharsets.UTF_8);
                    if (body.contains("Exception")
                            || body.contains("StackTrace")
                            || body.contains("SELECT ")
                            || body.contains("at com.example")) {
                        throw new AssertionError("Unsafe error response");
                    }
                });
    }

    private void assertEqualsBigDecimal(String expected, Object actual) {
        if (actual == null
                || new BigDecimal(expected).compareTo(new BigDecimal(actual.toString())) != 0) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    private boolean containsOwnId(UUID[] ownIds, String id) {
        for (UUID ownId : ownIds) {
            if (ownId.toString().equals(id)) {
                return true;
            }
        }
        return false;
    }
}

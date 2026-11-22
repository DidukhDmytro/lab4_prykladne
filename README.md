# Car Service

A Java 25 REST application built with Spring Boot 4.1.2 for managing customers, cars,
mechanics, repair orders, service works, and pricing in PostgreSQL.

## Requirements

- JDK 25
- Apache Maven
- Docker for PostgreSQL 18 and Testcontainers

## Run locally

Start PostgreSQL:

```sh
docker compose up -d
```

Run the application:

```sh
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. The local database is
`jdbc:postgresql://localhost:5432/carservice` with username `carservice` and password
`carservice`. `DB_URL` and `DB_USERNAME` override the database URL and username.

Stop PostgreSQL with `docker compose down`. Recreate it with
`docker compose down -v && docker compose up -d`.

## REST API

| Method | Path | Response |
|---|---|---|
| `POST` | `/api/customers` | `201`, `CustomerResponse`, `Location: /api/customers/{id}` |
| `GET` | `/api/customers/{id}` | `200`, `CustomerResponse` |
| `POST` | `/api/cars` | `201`, `CarResponse`, `Location: /api/cars/{vin}` |
| `GET` | `/api/cars/{vin}` | `200`, `CarResponse` |
| `POST` | `/api/mechanics` | `201`, `MechanicResponse`, `Location: /api/mechanics/{id}` |
| `GET` | `/api/mechanics/{id}` | `200`, `MechanicResponse` |
| `POST` | `/api/orders` | `201`, `OrderResponse`, `Location: /api/orders/{id}` |
| `GET` | `/api/orders/{id}` | `200`, `OrderResponse` |
| `GET` | `/api/orders?status=&page=&size=&sort=` | `PageResponse<OrderResponse>` |
| `POST` | `/api/orders/{id}/works` | `201`, `OrderResponse` |
| `POST` | `/api/orders/{id}/works/batch` | `201`, `OrderResponse` |
| `POST` | `/api/orders/{id}/works/{index}/complete` | `200`, `OrderResponse` |
| `PUT` | `/api/orders/{id}/mechanic` | `200`, `OrderResponse` |
| `PUT` | `/api/orders/{id}/priority` | `200`, `OrderResponse` |
| `POST` | `/api/orders/{id}/diagnose` | `200`, `OrderResponse` |
| `POST` | `/api/orders/{id}/approve` | `200`, `OrderResponse` |
| `POST` | `/api/orders/{id}/start` | `200`, `OrderResponse` |
| `POST` | `/api/orders/{id}/complete` | `200`, `OrderResponse` |
| `POST` | `/api/orders/{id}/cancel` | `200`, `OrderResponse` |
| `GET` | `/api/orders/{id}/price` | `200`, `PriceResponse` |
| `GET` | `/api/orders/{id}/present` | `200`, `PresentResponse` |

Order status changes use explicit POST action endpoints. There is no generic status-change
endpoint and no PUT/PATCH status endpoint.

## Status codes and ProblemDetails

Errors use Spring RFC 9457 `application/problem+json` responses. The body includes `status`,
`title`, `detail`, `type`, and `instance`.

- `400 Bad Request`: Bean Validation failures, malformed JSON, invalid enum/path values,
  invalid pagination values (including negative or non-numeric pages), constraint violations,
  invalid sort properties, and invalid arguments.
- `404 Not Found`: unknown entities and unknown routes.
- `409 Conflict`: duplicate data, data-integrity conflicts, optimistic-lock conflicts, and
  domain/business-rule violations.
- `500 Internal Server Error`: generic unexpected failures without SQL, stack traces, or class names.

Business-rule violations return `409 Conflict`, not `422 Unprocessable Content`, because the
request is syntactically valid and passes validation; the current state of the resource prevents
the requested operation.

## Pagination and sorting

`GET /api/orders` accepts an optional `status`, `page`, `size`, and `sort`.

- Default page size: 20
- Maximum page size: 50; values above 50 are clamped to 50
- Size 0: defaults to 20
- Default sort: `id,asc`
- Sort whitelist: `status`, `priority`, `finalPrice`, `completedAt`, `id`
- `id,asc` is appended as a tie-breaker unless `id` is already requested
- Multiple sort parameters are supported
- Invalid page numbers, status values, and sort properties return `400 Bad Request` ProblemDetails

The service paginates repair-order IDs first, then loads the associated works and relationships
with an entity graph and restores page order. It does not use collection `join fetch` pagination.

## HTTP request collection

The IntelliJ `.http` collection is under `http/`. Select the `local` environment from
`http/http-client.env.json`, which defines `baseUrl=http://localhost:8080`. The scripts store
created IDs in variables where later requests depend on them. Registration uses a unique VIN on
each run; the negative-scenario script registers its own customer, car, and mechanic.

## Test levels

Docker is required for integration and E2E levels.

```sh
mvn test                    # unit tests only
mvn verify -Pintegration    # integration tests only
mvn verify -Pe2e            # end-to-end tests only
mvn -q clean verify         # unit + integration + E2E
```

Unit tests use `*Test` and contain no Spring application context or Docker containers. Integration
tests use `*IT` in `com.example.carservice.integration`. E2E tests use `*E2EIT` in
`com.example.carservice.e2e`, run against a random-port server, and do not inject beans or
repositories or use test transactions.

Integration tests use the fixed Clock from `IntegrationTestConfiguration` and PostgreSQL
Testcontainers. Both integration and E2E tests reuse
`PostgreSqlTestContainerConfiguration`. E2E and integration data is isolated by test-created,
UUID-based identifiers and test cleanup rules; tests do not assume exact global totals.

## Project structure

```text
src/
├── main/java/com/example/carservice/
│   ├── api/               # Controllers, record DTOs, mappers, paths, and ProblemDetail advice
│   ├── app/               # Standalone pricing-rule examples
│   ├── config/            # Pricing and Clock configuration
│   ├── domain/            # JPA entities and business invariants
│   ├── exception/         # Domain/application exceptions and messages
│   ├── pricing/           # Pricing policy, context, engine, and rules
│   ├── repository/        # Spring Data repository interfaces
│   └── service/           # Transactional application services
├── main/resources/
│   └── db/migration/      # Flyway migrations
└── test/java/com/example/carservice/
    ├── api/               # REST unit tests
    ├── domain/            # Domain invariant tests
    ├── e2e/               # Random-port E2E tests
    ├── integration/       # PostgreSQL integration tests
    ├── pricing/           # Pricing unit tests
    ├── service/           # Service unit tests
    └── support/           # Shared Testcontainers configuration
```

## Architecture

- **Controller/mapper boundary:** controllers accept record DTOs and call services only.
  `@Component` mappers convert domain objects to responses and requests to domain objects.
- **Services:** `RegistrationService` and `RepairOrderService` own transactions and lookup rules.
- **Domain:** entities enforce invariants and state transitions.
- **Repositories:** Spring Data JPA provides persistence. Car lookup fetches its owner; repair-order
  lookup fetches relationships needed outside a transaction.
- **Pagination:** the repair-order list is two-step: one database page of IDs, then an entity graph
  load of those IDs with page-order restoration.
- **Pricing:** `PricingPolicy` is the service-facing abstraction; `PriceCalculator` applies
  ordered pricing rules and returns a rounded final price.

## Database schema

Flyway owns the schema and Hibernate validates it. V1 creates customers, cars, mechanics,
repair orders, and service works. V2 and V3 add optimistic-lock versions for repair orders and
mechanics.

# Project Guidelines

Apply these rules to every file generated or modified in this project:

- Use `com.example.carservice` as the root Java package; place project classes in that package or its domain-appropriate subpackages.
- Do not use `final` on classes, methods, parameters, or local variables. `final` is allowed and required on constants (`static final`) and dependency fields injected through Lombok `@RequiredArgsConstructor`.
- Constants use UPPER_SNAKE_CASE.
- Use strict Java naming conventions: UpperCamelCase for classes and lowerCamelCase for methods and fields. Keep fields private and expose only the accessors or operations the domain requires.
- Write all code comments and Javadoc strictly in English. Keep comments clear and concise, and use them to explain business rules where needed.
- Do not hardcode magic strings or numbers in output or exception checks. Define error messages, console output formats, and default constants as static variables.
- Keep code minimal and free of unnecessary, dead, or unused declarations.

## Lab 4 rules

- Stack: Java 25, PostgreSQL 18, Lombok, Maven, JUnit 5, Mockito, Flyway, and Testcontainers.
- Keep Spring Boot 4.1.2; do not upgrade to 4.2 RC.
- Boot 4 specifics: use `spring-boot-starter-webmvc` and `spring-boot-starter-validation`; Jackson 3 types use `tools.jackson.*` packages, while Jackson annotations remain in `com.fasterxml.jackson.annotation`; test autoconfiguration may be in new Boot 4 modules/packages.
- Never guess imports. If code or tests do not compile, verify it against the Spring Boot 4.1 documentation and migration guide.
- Use the layering controller -> service -> domain. Controllers must not use repositories or JPA entities; expose only record DTOs and map entities/domain objects in `@Component` mappers.
- Use Lombok `@RequiredArgsConstructor` and `@Slf4j` instead of hand-written injection constructors in new classes.
- Define API paths, messages, and error titles in constants such as `ApiPaths`, `ApiMessages`, and API error constants.
- Do not add generic status-change endpoints. Use explicit `POST` action endpoints for transitions.
- Test conventions: unit tests use `*Test`, integration tests use `*IT` in package `integration`, and E2E tests use `*E2EIT` in package `e2e`.
- E2E tests must not inject beans or repositories and must not use `@Transactional`.
- Do not change existing domain invariants or existing tests unless a task explicitly says otherwise.
- Run `mvn -q clean verify` after each task.

Test and build structure:
- Unit tests use `*Test` and run with Surefire.
- Integration tests use `*IT` in package `integration` and run with the `integration` Maven profile.
- E2E tests use `*E2EIT` in package `e2e` and run with the `e2e` Maven profile.
- `mvn verify` runs all test levels.

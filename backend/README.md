# Backend

This directory contains the Java 25 and Spring Boot 4.1 modular-monolith application.

R0-003 established the backend engineering foundation. R0-004 adds MySQL,
JPA, and Flyway integration. No business APIs, authentication, entities, or
production business tables exist yet. See the
[R0-004 final review](../docs/reviews/R0-004.md).

## Project identity

| Setting | Value |
|---|---|
| Maven group | `com.retail` |
| Maven artifact | `retail-management` |
| Base Java package | `com.retail.retailmanagement` |
| Spring application name | `retail-management` |

## Prerequisites

- A Java 25 JDK must be active in the current shell.
- A functioning Docker Engine is required for integration tests. Testcontainers
  starts `mysql:8.4.11` automatically; a manually started MySQL service or Compose
  stack is not required for tests. The first run needs access to the image registry.
- A global Maven installation is not required and must not be used as the project build contract.

Verify the toolchain from this directory:

```bash
java --version
./mvnw --version
```

The Maven build enforces Java 25 and rejects other Java major versions.

## Build and test

Run the complete backend verification lifecycle from this directory:

```bash
./mvnw clean verify
```

This command compiles the application, runs the automated tests, creates the executable JAR, and applies build-time enforcement rules.

The tests use JUnit and a shared Spring-managed MySQL container configuration.
They verify database identity, Flyway history, JDBC write/read behavior, Hibernate
validation, persistence-context boundaries, and HTTP health/security behavior.
The test account password is generated for each container. Missing Docker is a
verification failure, not a reason to skip database tests.

Tests enforce readiness membership and ensure Open EntityManager in View remains
disabled. Hibernate has no domain entities to validate yet; mapping/schema
compatibility tests belong with the first domain model.

## Run locally

Provide a reachable MySQL 8.4 database and external datasource configuration
using the [database setup guide](../docs/setup/database.md), then start the
application with the Maven Wrapper:

```bash
./mvnw spring-boot:run
```

The application listens on port `8080` unless external configuration overrides it.

## Health endpoints

The following anonymous HTTP endpoints are available:

| Purpose | Method and path |
|---|---|
| Liveness | `GET /actuator/health/liveness` |
| Readiness | `GET /actuator/health/readiness` |

For example:

```bash
curl --fail http://localhost:8080/actuator/health/liveness
curl --fail http://localhost:8080/actuator/health/readiness
```

A healthy response contains only the aggregate status:

```json
{"status":"UP"}
```

Health components and details are intentionally hidden.

Readiness includes `db` and `readinessState`, while liveness is limited to
`livenessState`. A database failure makes readiness return HTTP 503 without
making liveness fail.

## Security baseline

Only anonymous `GET` requests to the liveness and readiness paths are permitted. Every other request is denied by the current security filter chain, including the root health endpoint and other actuator paths.

Form login and HTTP Basic authentication are disabled. R0-003 does not create a default user or generated password. Authentication and the application's authorization model belong to R1.

## Logging

The default profile emits human-readable console logs for local development.

Activate the structured logging profile when machine-readable output is required:

```bash
SPRING_PROFILES_ACTIVE=json-logging ./mvnw spring-boot:run
```

The `json-logging` profile disables the startup banner and emits one Logstash-compatible JSON object per log event. Log output goes to standard output; log collection and retention infrastructure are not part of R0-003.

## Ownership

The backend will own:

- HTTP application APIs;
- authentication and authorization enforcement;
- business capabilities and transaction boundaries;
- persistence through the platform's single MySQL schema;
- database migrations;
- server-side health, logs, metrics, and traces.

Business code will be organized by capability rather than by global technical layers. A capability may use another capability only through an explicit module-facing application interface; it must not reach into another capability's entities, repositories, or other internal implementation types.

The current backend introduces database integration without business capabilities.
The smoke table and its `V0` migration exist only in test resources. Production
migrations start at `V1` when a real domain schema is introduced.

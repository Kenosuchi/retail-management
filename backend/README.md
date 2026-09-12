# Backend

This directory contains the Java 25 and Spring Boot 4.1 modular-monolith application.

R0-003 establishes only the backend engineering foundation. It does not introduce business APIs, authentication, or persistence.

## Project identity

| Setting | Value |
|---|---|
| Maven group | `com.retail` |
| Maven artifact | `retail-management` |
| Base Java package | `com.retail.retailmanagement` |
| Spring application name | `retail-management` |

## Prerequisites

- A Java 25 JDK must be active in the current shell.
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

## Run locally

Start the application with the Maven Wrapper:

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

The current backend has no business capabilities or database integration. Those concerns must be introduced only by their owning tickets while preserving the boundaries above.

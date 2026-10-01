# Current architecture

**Status:** Target architecture for the engineering foundation

**Last reviewed:** 2026-10-02

This document describes the platform's current intended structure and the rules against which implementation will be reviewed. Architecture decision records explain why consequential choices were made; they do not replace this current-state description.

## System context

```text
User's browser
      |
      | HTTPS / JSON
      v
Angular single-page application
      |
      | HTTP application API
      v
Spring Boot modular monolith
      |
      | JDBC / managed transactions
      v
Single MySQL schema
```

The frontend and backend are separate build artifacts in one repository. The backend is one runtime and one deployable unit. Business capabilities execute in-process and share one database ownership boundary.

The diagram is the current logical target for R0. The Spring Boot backend
foundation exists as of R0-003, R0-004 adds the MySQL persistence foundation,
R0-005 adds the Angular application foundation, and R0-006 supplies their
local Compose integration.

## Component responsibilities

### Frontend

The Angular application owns presentation, browser interaction, client-side navigation, and calls to documented backend APIs. It does not connect directly to MySQL or contain trusted secrets.

Frontend code will be organized around user-facing features as they are introduced. Common code is extracted only after its reuse and semantics are understood; a generic shared area is not a default dependency for every feature.

The R0-005 implementation is a standalone, strict, zoneless Angular 21
application. Its root outlet activates a common application shell, whose child
routes provide the foundation page and wildcard not-found behavior. Route
components are loaded lazily, and Vitest covers rendered content, navigation,
and document titles. No business feature, backend API client, authentication
state, or browser-delivered environment configuration exists yet.

### Backend

The Spring Boot application owns business behavior, security enforcement, transaction boundaries, persistence, and operational endpoints. It will be organized by business capability, not as repository-wide `controller`, `service`, and `repository` packages.

A capability's internal package structure may evolve with its needs. The architecture does not require every capability to contain identical technical subpackages.

The backend contains the application bootstrap, security baseline, health
endpoints, and the R0-004 persistence integration. No business capability,
entity, or repository exists yet. The [R0-004 final review](../reviews/R0-004.md)
records the verification evidence.

### Database

MySQL is the primary source of truth. The modular monolith owns one schema, while each future business capability owns the tables and persistence model associated with that capability.

Sharing a schema does not permit one capability to manipulate another capability's tables or persistence entities directly. Cross-capability behavior goes through an explicit module-facing application interface.

Flyway owns schema evolution. Hibernate is configured to validate mappings
rather than create or update production-like schemas. MySQL 8.4 LTS is the
supported server line; [ADR-0004](../adr/0004-mysql-8-4-lts-baseline.md) records
the choice. A Spring-managed Testcontainers MySQL instance provides an isolated
database for integration tests. The test-only `V0` migration is absent from the
production artifact; production migrations will start at `V1`.

### Infrastructure

Infrastructure definitions support the application; they do not contain business logic. R0 does not include Redis, Kafka, Kubernetes, Elasticsearch, or a service mesh.

R0-006 defines a local Compose environment with three containers: a static
Angular build served by Nginx, the single Spring Boot deployable, and MySQL.
Compose service discovery connects the backend to MySQL on its private network;
only frontend and backend ports are bound to the loopback interface. The MySQL
named volume persists local data independently of database containers. This is
local integration infrastructure, not a production topology or a microservice
boundary.

## Backend module-boundary rules

When business capabilities are added:

1. Code is grouped first by business capability.
2. A capability exposes only the smallest application-facing interface needed by callers.
3. Another capability must not import its internal entities, repositories, controllers, or persistence adapters.
4. Database relationships do not justify cross-capability repository access.
5. In-process calls are the default. Domain or application events require a concrete decoupling need; an event bus is not assumed.
6. Transactions remain local to the single application unless a future architecture decision explicitly changes that constraint.
7. Generic shared-domain models are avoided. Truly cross-cutting technical facilities may be centralized, but domain meaning stays with the owning capability.

These are source-code dependency rules, not a requirement to introduce Java Platform Module System, Spring Modulith, or a custom framework in R0. Automated boundary enforcement can be considered after real modules exist.

## Security boundary

The backend is the trust boundary for application data and authorization. The frontend may improve usability by hiding unavailable actions, but it cannot enforce authorization by itself.

Authentication design belongs to R1. Until then, architecture documentation must not imply a selected token format, identity provider, or authorization model.

During R0-003, only anonymous `GET` requests to the liveness and readiness health paths are permitted. All other HTTP requests are denied, and form login and HTTP Basic authentication are disabled. This temporary foundation is not the authentication design for R1.

Secrets are supplied through external configuration. They must not be committed to the repository, built into browser assets, or included in container images.

## Observability ownership

Logging, metrics, tracing, and health checks are platform-level capabilities exposed consistently by the single backend runtime. Business capabilities contribute meaningful domain context through those facilities rather than selecting their own logging or monitoring stacks.

The R0-003 backend provides human-readable console logs by default, an opt-in Logstash-compatible JSON console profile, and liveness and readiness health endpoints. Other actuator endpoints are not publicly exposed.

The R0-004 readiness contract includes database availability because no degraded
operating mode exists. Liveness remains independent of MySQL, avoiding restarts
in response to a shared database outage. All health details remain private.

The monitoring backend, retention policy, alert rules, and production dashboards are deferred until requirements are known. No observability vendor or search platform is implied by using structured logs.

## Change governance

The modular monolith remains the default. A microservice extraction requires an architecture review and a new ADR that demonstrates at least one concrete benefit, such as:

- an independently owned bounded context;
- materially different scaling requirements;
- an independent deployment cadence with measurable value;
- failure-isolation or regulatory requirements that cannot be met reasonably in-process.

An extraction proposal must also account for data ownership, consistency, failure handling, observability, security, deployment, and operational cost. Code size or the availability of distributed-system technology is not sufficient justification.

## Related decisions

- [ADR-0001: Modular monolith and monorepo](../adr/0001-modular-monolith-and-monorepo.md)
- [ADR-0002: Repository-controlled build tooling](../adr/0002-repository-controlled-build-tooling.md)
- [ADR-0003: Flyway-managed database migrations](../adr/0003-flyway-managed-database-migrations.md)
- [ADR-0004: MySQL 8.4 LTS baseline](../adr/0004-mysql-8-4-lts-baseline.md)

# Current architecture

**Status:** Target architecture for the engineering foundation

**Last reviewed:** 2026-09-12

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

The diagram is the current logical target for R0. The Spring Boot backend foundation exists as of R0-003. The Angular application, MySQL integration, and supporting infrastructure are introduced by later tickets.

## Component responsibilities

### Frontend

The Angular application owns presentation, browser interaction, client-side navigation, and calls to documented backend APIs. It does not connect directly to MySQL or contain trusted secrets.

Frontend code will be organized around user-facing features as they are introduced. Common code is extracted only after its reuse and semantics are understood; a generic shared area is not a default dependency for every feature.

### Backend

The Spring Boot application owns business behavior, security enforcement, transaction boundaries, persistence, and operational endpoints. It will be organized by business capability, not as repository-wide `controller`, `service`, and `repository` packages.

A capability's internal package structure may evolve with its needs. The architecture does not require every capability to contain identical technical subpackages.

At R0-003, the backend contains only the application bootstrap, security baseline, and health endpoints. No business capability or persistence integration exists yet.

### Database

MySQL is the primary source of truth. The modular monolith owns one schema, while each future business capability owns the tables and persistence model associated with that capability.

Sharing a schema does not permit one capability to manipulate another capability's tables or persistence entities directly. Cross-capability behavior goes through an explicit module-facing application interface.

Flyway will own schema evolution. Hibernate will validate mappings rather than create or update production-like schemas.

### Infrastructure

Infrastructure definitions support the application; they do not contain business logic. R0 does not include Redis, Kafka, Kubernetes, Elasticsearch, or a service mesh.

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

# ADR-0001: Modular monolith and monorepo

**Status:** Accepted

**Date:** 2026-09-10

## Context

The Retail Management Platform is beginning without existing business modules, independent component owners, or evidence that different capabilities need separate scaling or release schedules. The system must support realistic production engineering while remaining understandable and operable by one engineering team.

Frontend, backend, database, infrastructure, and documentation will evolve together during the early releases. Many changes will need atomic review across those concerns.

A single deployable can still become tightly coupled if its source boundaries are organized only by technical layer. The choice of a monolith therefore needs explicit internal modularity rules.

## Decision

The platform will start as a modular monolith in one Git repository.

The repository contains one Angular frontend and one Spring Boot backend. The backend is one runtime and one deployable unit backed by one MySQL schema. Future backend code is grouped by business capability, and each capability hides its internal domain and persistence implementation.

Capabilities communicate in-process through explicit module-facing application interfaces. A capability does not directly use another capability's repository, persistence entity, controller, or internal implementation type merely because they share a process or schema.

The initial repository topology is:

```text
backend/
frontend/
infra/
docs/
```

Microservice extraction is not an incremental refactoring performed silently. It requires a separate architecture review and ADR covering ownership, data, consistency, deployment, failure modes, security, and operations.

## Alternatives considered

### Traditional layered monolith

Organizing the entire backend into global controller, service, repository, and entity layers is familiar and initially simple. It was not selected because business boundaries become implicit, unrelated capabilities accumulate dependencies, and later ownership or extraction becomes harder to reason about.

### Multiple repositories for frontend and backend

Separate repositories can support independent teams, permissions, and release lifecycles. Those benefits are not currently present. Separate repositories would make atomic API and CI changes more difficult for one team.

### Microservices from the beginning

Microservices can provide independent deployment, scaling, ownership, and failure isolation. The current project has no evidence that those benefits outweigh distributed transactions, network failures, duplicated operational tooling, versioned contracts, and substantially greater deployment complexity.

## Consequences

### Positive

- Cross-cutting changes can be reviewed and versioned atomically.
- Business transactions can initially use local database transactions.
- Local development, testing, deployment, and observability remain comparatively simple.
- Capability boundaries can be learned from real domain behavior before distribution decisions are made.

### Negative

- The backend must be deployed and scaled as one unit.
- Weak source boundaries could allow accidental coupling despite the architecture name.
- A failure that exhausts shared process resources can affect every capability.
- Later extraction will require deliberate data and API separation work.

## Risks

- A generic shared package could become an uncontrolled dependency hub. Reviews must require stable ownership and demonstrated reuse before extracting shared domain code.
- Direct cross-capability database access could conceal coupling. Persistence types and repositories remain internal to their owning capability.
- Speculative module frameworks could add ceremony without enforcing useful boundaries. Automated enforcement is deferred until real business modules exist.

## Revisit triggers

- Separate teams acquire durable ownership of distinct bounded contexts.
- A capability needs materially different scaling or availability characteristics.
- Independent deployment cadence provides measurable delivery value.
- Regulatory, data-residency, or failure-isolation requirements cannot reasonably be met in-process.
- Operational evidence shows that the costs of the single deployable exceed the costs of distribution.

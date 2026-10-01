# Retail Management Platform

This repository contains the Retail Management Platform. The project is currently in **R0 — Engineering Foundation**; business functionality has not been introduced yet.

## Developer setup

Before working on a component, complete the [toolchain and preflight checks](docs/setup/toolchain.md) and follow that component's README.

Project builds use repository-owned wrappers and package scripts as they are introduced. The backend must be built with its committed Maven Wrapper. A globally installed Maven, Gradle, or Angular CLI is not part of the build contract.

## Architecture

The platform starts as a modular monolith with one Angular frontend, one Spring Boot backend deployable, and one MySQL database ownership boundary. See the [current architecture](docs/architecture/README.md) and [architecture decision records](docs/adr/README.md).

## Repository layout

| Path | Responsibility |
|---|---|
| [`backend/`](backend/README.md) | Spring Boot application and future business modules |
| [`frontend/`](frontend/README.md) | Angular browser application |
| [`infra/`](infra/README.md) | Local and deployment infrastructure definitions |
| [`docs/architecture/`](docs/architecture/README.md) | Current intended system structure and boundary rules |
| [`docs/adr/`](docs/adr/README.md) | History and rationale for consequential technical decisions |
| [`docs/setup/`](docs/setup/toolchain.md) | Developer workstation and setup documentation |

## Current implementation boundary

R0-003 through R0-006 establish the backend, persistence, frontend, and local
container foundations. They currently provide:

- a Java 25 and Spring Boot 4.1 backend built with the Maven Wrapper;
- secure-by-default HTTP access rules;
- anonymous liveness and readiness probes;
- human-readable local logging and an opt-in structured JSON logging profile;
- MySQL 8.4 persistence through JPA/Hibernate and Flyway-managed migrations;
- database-aware readiness with database-independent liveness;
- JUnit integration tests using a disposable MySQL container;
- a strict, standalone, zoneless Angular 21 application built with the
  repository-local Angular CLI;
- an application shell with a foundation route, not-found handling, route
  titles, and Vitest coverage;
- a Docker Compose local integration environment with an Angular production
  build, Spring Boot backend, and persistent MySQL 8.4 database.

See the [R0-004 final review](docs/reviews/R0-004.md) and
[database setup](docs/setup/database.md). Frontend setup and verification are
documented in the [frontend README](frontend/README.md). Local container
startup, bounded smoke checks, health checks, logs, persistence, and safe reset
procedures are in the [infrastructure guide](infra/README.md); the
[R0-006 final review](docs/reviews/R0-006.md) records its acceptance evidence.

Business APIs, authentication, production business migrations, frontend
features, backend API integration, CI, staging deployment, and production
operations belong to subsequent tickets.

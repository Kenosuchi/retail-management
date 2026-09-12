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

R0-003 introduces the Spring Boot backend foundation. It currently provides:

- a Java 25 and Spring Boot 4.1 backend built with the Maven Wrapper;
- secure-by-default HTTP access rules;
- anonymous liveness and readiness probes;
- human-readable local logging and an opt-in structured JSON logging profile;
- automated tests for the security and health contracts.

Business APIs, authentication, persistence, database migrations, frontend scaffolding, containers, and CI belong to subsequent tickets.

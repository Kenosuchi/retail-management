# Retail Management Platform

This repository contains the Retail Management Platform. The project is currently in **R0 — Engineering Foundation**; business functionality has not been introduced yet.

## Developer setup

Before adding application code, complete the [toolchain and preflight checks](docs/setup/toolchain.md).

Project builds must use repository-owned wrappers and package scripts once they are introduced. A globally installed Maven, Gradle, or Angular CLI is not part of the build contract.

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

R0-002 covers architecture and repository documentation only. Backend and frontend scaffolding, database configuration, containers, and CI belong to subsequent tickets.

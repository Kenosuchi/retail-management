# ADR-0002: Repository-controlled build tooling

**Status:** Accepted

**Date:** 2026-09-10

## Context

The backend and frontend require different ecosystems, but both must build consistently on developer workstations and clean CI runners. Depending on globally installed build tools allows machine-specific versions to change resolution, generated output, and available commands.

The backend build-tool choice should optimize for a conventional Spring workflow and transparent dependency management. The frontend must keep the Angular CLI aligned with the framework and make dependency installation deterministic.

## Decision

The backend will use Maven through the Maven Wrapper committed with the backend project. The wrapper will select the Maven version, and the build will target Java 25. Spring Boot dependency management will define compatible Spring ecosystem versions; explicit overrides require a documented reason.

The frontend will install Angular CLI and all build dependencies locally. Supported operations will be exposed as npm scripts. The npm lockfile will be committed, and CI will use clean lockfile-based installation.

Global Maven, Gradle, Angular CLI, or globally cached dependencies are not part of the build contract. Root-level documentation may coordinate component commands, but R0 will not introduce another build system merely to wrap Maven and npm.

## Alternatives considered

### Gradle with the Gradle Wrapper

Gradle offers a programmable build model, strong support for sophisticated multi-project builds, and useful build-performance features. It was not selected because this project does not currently need custom build logic, and Maven's conventional lifecycle reduces build-language complexity while the domain and Spring stack are being learned.

### Globally installed Maven and Angular CLI

Global installations reduce the number of repository files and can feel convenient on one workstation. They were rejected because they do not provide a repeatable version contract for contributors or CI.

### Container-only builds

Building exclusively inside containers can strongly isolate toolchains. It was not selected as the only workflow because it slows common development feedback and makes basic builds depend on Docker. Container builds may still be used for image creation and smoke verification.

### Unlocked frontend dependency installation

Allowing npm to resolve dependency ranges afresh provides rapid access to patches. It was rejected for CI because identical source revisions could resolve different dependency graphs and fail non-deterministically.

## Consequences

### Positive

- Developers and CI invoke the same Maven distribution and project commands.
- The Angular CLI version remains aligned with the frontend dependency graph.
- Committed lockfiles make dependency changes visible in code review.
- Contributors do not need global Maven or Angular CLI installations.

### Negative

- Wrapper files and lockfiles must be maintained and reviewed.
- Maven offers less flexible custom build logic than Gradle without plugins.
- Locked dependencies do not receive fixes until an explicit update is made.
- Java and Node runtimes still need a separate workstation or CI provisioning mechanism.

## Risks

- Wrapper distribution configuration or dependency repositories could become supply-chain entry points. Their URLs and version changes must be reviewed.
- Developers may bypass npm scripts with a global CLI and get different behavior. Documentation and CI must use only repository-controlled commands.
- Unnecessary version overrides could escape Spring Boot's tested dependency set. Overrides require justification and targeted tests.

## Revisit triggers

- The backend becomes a sufficiently complex multi-project build that Gradle provides measured maintainability or performance benefits.
- Organization-wide build infrastructure mandates a different tool or artifact-provenance mechanism.
- npm no longer meets the frontend's deterministic installation or security requirements.

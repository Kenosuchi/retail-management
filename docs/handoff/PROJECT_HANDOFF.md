# Retail Management Platform project handoff

**Prepared:** 2026-10-02 (Asia/Saigon)

**Repository baseline:** `main` at `d3c4502` (`R0-006 — Establish the local
Docker Compose environment.`)

This document is intended to let a new Codex session continue the project
without relying on the previous conversation. It does not introduce application
behavior or implement the current ticket.

Evidence is identified as follows:

- **Repository fact** means the statement was verified from committed files,
  Git history, or a command run against the current checkout.
- **Conversation context** means the statement came from the working agreement
  or ticket refinement in the previous Codex session and is not necessarily
  stored elsewhere in the repository.
- **Unverified** means external GitHub settings or another machine's state could
  not be inspected from this checkout.

## 1. Project Overview

The project is a production-oriented Retail Management Platform. The long-term
business direction is to support identity, product catalog, inventory,
purchasing, sales orders, and the operational practices required to deploy and
run the platform safely. **Conversation context:** the project is also a
learning environment. Ticket work and reviews should develop the engineering
judgment expected from a strong full-stack or senior software engineer, rather
than optimize only for fast feature delivery.

The agreed roadmap is **conversation context**:

1. R0 — Engineering Foundation
2. R1 — Identity and Product Catalog
3. R2 — Inventory and Purchasing
4. R3 — Sales Orders and Redis
5. R4 — Production Readiness
6. R5 — Microservice Architecture Review and first justified extraction
7. R6 — Distributed System Hardening

**Repository fact:** the project remains in R0. It currently has engineering
foundations but no retail business API, identity model, product catalog,
inventory, purchasing, or sales-order behavior.

The technical-lead working agreement is **conversation context**:

- Start as a modular monolith and challenge unnecessary complexity.
- Do not introduce microservices without an explicit architecture review and
  ADR demonstrating a concrete boundary, ownership, scaling, deployment, or
  isolation benefit.
- Do not introduce Kafka, Kubernetes, Elasticsearch, or other infrastructure
  without a concrete requirement.
- Refine one ticket at a time. The engineer implements it; the Technical Lead
  reviews correctness, maintainability, architecture, modeling, security,
  concurrency, transactions, performance, error handling, tests,
  observability, and production readiness.
- Review findings use `BLOCKER`, `MAJOR`, `MINOR`, and `SUGGESTION`.
- A ticket is complete only when its acceptance criteria and Definition of Done
  are satisfied. Do not silently implement future tickets.

## 2. Current Architecture and Tech Stack

### Architecture

**Repository fact:** ADR-0001 establishes a modular monolith in a monorepo:

```text
Browser
  -> Angular single-page application
  -> future HTTP/JSON application API
  -> one Spring Boot modular-monolith runtime
  -> JDBC / managed transactions
  -> one MySQL schema
```

The Angular frontend and Spring Boot backend are separate build artifacts but
are versioned together. Future backend code must be grouped by business
capability rather than repository-wide controller/service/repository layers.
Capabilities may call one another only through explicit module-facing
interfaces and must not import another capability's repositories, entities,
controllers, or internal implementations.

### Implemented technology

| Area | Repository state |
|---|---|
| Frontend | Angular 21.2, TypeScript 5.9, standalone strict and zoneless application, Angular Router, SCSS |
| Frontend tests | Vitest through Angular's unit-test builder, Angular TestBed, router testing harness, jsdom |
| Backend | Java 25, Spring Boot 4.1.1, Spring MVC, Spring Security, Actuator, Validation |
| Persistence | Spring Data JPA/Hibernate, Flyway, MySQL Connector/J |
| Database | MySQL 8.4 LTS; tests and Compose pin `mysql:8.4.11` |
| Backend tests | JUnit 5/Spring Test, MockMvc, Testcontainers MySQL |
| Containers | Docker, Docker Compose, multi-stage backend and frontend images, unprivileged runtime users |
| Frontend runtime image | Nginx unprivileged `1.31.6-alpine3.24` serving the Angular production build |
| Logging and health | Human-readable backend logs by default, opt-in Logstash-compatible JSON logs, liveness and readiness probes |
| CI/CD | Not implemented; R0-007 is the current planned ticket |

**Repository fact:** Redis, Kafka, Kubernetes, Elasticsearch, a service mesh,
microservices, cloud deployment, staging, production deployment, backup/restore,
and monitoring infrastructure are absent. Redis remains roadmap direction for
R3 only when a justified use case exists.

### Backend runtime contract

- Maven coordinates: `com.retail:retail-management:0.0.1-SNAPSHOT`.
- Current base Java package: `com.retail.retailmanagement`.
- Spring application name: `retail-management`.
- Only anonymous `GET /actuator/health/liveness` and
  `GET /actuator/health/readiness` are allowed.
- Every other request is denied. Form login and HTTP Basic are disabled.
- Readiness includes `readinessState` and `db`; liveness includes only
  `livenessState`.
- Flyway owns schema evolution. Hibernate uses `ddl-auto=validate`, SQL init is
  disabled, Open EntityManager in View is disabled, Flyway validation is
  enabled, and Flyway clean is disabled.
- There are no production entities or migrations yet. Production migrations
  begin with `V1`; test-only migration `V0` creates a disposable smoke table.

### Frontend runtime contract

- `/` renders the foundation page and sets the title to `Retail Management`.
- Unknown browser routes render the not-found page and set the title to
  `Page not found`.
- The not-found recovery link navigates home.
- Nginx uses an SPA fallback for browser routes but returns HTTP 404 for missing
  known static-asset extensions.
- There is no backend API client, authentication state, browser-delivered
  environment configuration, or end-to-end browser test framework yet.

## 3. Repository Structure

```text
retail-management/
├── backend/                 Spring Boot application, Maven Wrapper and tests
│   ├── .mvn/wrapper/        Maven 3.9.16 wrapper configuration
│   ├── src/main/            bootstrap, security and runtime configuration
│   ├── src/test/            Testcontainers, persistence and security tests
│   ├── Dockerfile           Java 25 multi-stage non-root image
│   ├── pom.xml              backend dependency and build contract
│   └── README.md            backend setup, health and security behavior
├── frontend/                Angular application
│   ├── src/app/             shell, foundation page, not-found page and tests
│   ├── angular.json         Angular build/test targets and budgets
│   ├── package.json         npm scripts, engines and dependencies
│   ├── package-lock.json    deterministic dependency graph
│   ├── Dockerfile           Node build plus unprivileged Nginx runtime
│   ├── nginx.conf           SPA routing and static-asset behavior
│   └── README.md            frontend workflow and current boundaries
├── infra/
│   ├── docker-compose.yaml  frontend, backend and MySQL local environment
│   ├── .env.example         non-secret variable template
│   └── README.md            local operations and verification runbook
├── docs/
│   ├── adr/                 accepted architecture decisions and template
│   ├── architecture/        current architecture and module rules
│   ├── reviews/             final review evidence for R0-004 through R0-006
│   ├── setup/               toolchain and database instructions
│   └── handoff/             this handoff
├── .gitignore
├── .nvmrc                   Node 22.23.2
└── README.md                repository entry point and current scope
```

Important implementation files include:

- `backend/src/main/java/com/retail/retailmanagement/RetailManagementApplication.java`
  — Spring Boot entry point.
- `backend/src/main/java/com/retail/retailmanagement/security/SecurityConfig.java`
  — temporary deny-by-default R0 security baseline.
- `backend/src/main/resources/application.properties` — health, Flyway, JPA,
  application identity, and default logging configuration.
- `backend/src/main/resources/application-json-logging.properties` — opt-in
  structured console logging.
- `backend/src/test/java/com/retail/retailmanagement/MySqlTestConfiguration.java`
  — Spring-managed `mysql:8.4.11` Testcontainer with generated password.
- `backend/src/test/java/com/retail/retailmanagement/PersistenceFoundationTests.java`
  — MySQL identity, Flyway, JDBC round-trip, schema-validation, and persistence
  boundary tests.
- `backend/src/test/java/com/retail/retailmanagement/RetailManagementApplicationTests.java`
  — health and security integration tests through MockMvc.
- `frontend/src/app/app.routes.ts` and `frontend/src/app/app.spec.ts` — route
  contract and real application-fixture/router tests.

**Repository fact:** there is currently no `.github/workflows` directory and no
CI workflow.

## 4. Development Environment

The following versions were observed and verified on the source laptop on
2026-10-02. Only versions explicitly enforced or pinned by the repository are
portable requirements.

| Tool | Project requirement or pin | Source-laptop observation |
|---|---|---|
| Git | Supports normal clone/branch workflow | 2.53.0 |
| Java | JDK 25; Maven Enforcer accepts `[25,26)` | Eclipse Temurin 25.0.4+7 |
| Maven | Use repository wrapper | 3.9.16 through `backend/mvnw` |
| Node.js | Exact repository pin | 22.23.2 via `.nvmrc` and package engines |
| npm | Exact repository pin | 10.9.8 |
| Docker Engine | Running daemon accessible to the developer | 29.8.0 |
| Docker Compose | `docker compose` plugin | 5.5.1 |

The source laptop is Linux under WSL2, but WSL2 is an observation, not a project
requirement. Maven and Angular CLI must not be treated as required global tools:
use `backend/mvnw` and frontend npm scripts.

Docker is required for backend verification because Testcontainers starts a real
MySQL instance. A local MySQL server on port 3306 is not required for tests.

`infra/.env` exists on the source laptop, is intentionally ignored, and was not
read for this handoff. It contains machine-local configuration and must not be
committed or copied through source control. Create a new file on the destination
laptop from `infra/.env.example` and choose new local passwords.

The toolchain guide records a broken global Angular CLI installation on the
source laptop. This does not affect supported npm-script commands and should not
be reproduced as project configuration.

## 5. Completed Work

| Ticket | State | Evidence and delivered capability |
|---|---|---|
| R0-001 | Completed per conversation; supported by commit `b24ccd6` | Initial repository, root ignore rules, Node pin, root README and toolchain contract. No standalone final-review file exists. |
| R0-002 | Completed per conversation and commit `ec66efc` | Architecture documentation, ADR process, ADR-0001 through ADR-0003, and component documentation foundations. |
| R0-003 | Completed per conversation and commit `04da23c` | Java 25/Spring Boot backend, Maven Wrapper, security baseline, probes, structured logging profile and JUnit/MockMvc tests. No standalone final-review file exists. |
| R0-004 | Approved in `docs/reviews/R0-004.md`; commit `26a4340` | MySQL 8.4, JPA/Hibernate validation, Flyway, test-only V0 migration, Testcontainers and database integration tests, ADR-0004. |
| R0-005 | Approved in `docs/reviews/R0-005.md`; commit `22143e0` | Angular 21 application foundation, strict standalone/zoneless configuration, routing, Vitest tests, local npm scripts and production build. |
| R0-006 | Approved in `docs/reviews/R0-006.md`; commit `d3c4502` | Local Docker Compose environment, non-root application images, health checks, MySQL named volume, SPA-serving Nginx, and an operational local runbook. |

R0 as a release should not yet be called complete: R0-007 was refined as the
next ticket and has not been implemented.

Verification performed while preparing this handoff on 2026-10-02:

- Backend `./mvnw --batch-mode --no-transfer-progress clean verify`: passed,
  16 tests, zero failures/errors/skips, using Testcontainers MySQL 8.4.11.
- Frontend `npm run verify`: passed Prettier, 6 Vitest tests across 4 files, and
  the production Angular build (203.37 kB initial raw bundle).
- `docker compose --env-file infra/.env.example -f infra/docker-compose.yaml
  config --quiet`: passed.

The first backend attempt inside Codex's restricted sandbox failed because the
sandbox denied `/var/run/docker.sock`. The same command passed when granted
Docker access. This is an execution-environment restriction, not a repository
test failure.

## 6. Current Ticket and Implementation Status

### R0-007 — Establish the GitHub Actions CI quality gate

**Source:** conversation-only ticket refinement. It has not yet been committed
as a ticket document. The repository has no CI implementation.

### Objective

Create a secure, deterministic GitHub Actions workflow that validates pull
requests, pushes to `main`, and manual runs using the repository-controlled
backend, frontend, and container build contracts.

### Scope and requirements

1. Add a workflow under `.github/workflows/` with triggers for:
   - `pull_request`;
   - pushes to `main`;
   - `workflow_dispatch`.
2. Give the workflow minimum permissions, normally `contents: read`.
3. Do not use `pull_request_target`.
4. Pin external actions to full immutable commit SHAs and annotate each pin with
   its human-readable release version.
5. Add independently visible jobs for:
   - backend verification using Java 25 and
     `./mvnw --batch-mode --no-transfer-progress clean verify` from `backend/`;
   - frontend verification using Node 22.23.2, npm 10.9.8, `npm ci`, and
     `npm run verify` from `frontend/`;
   - Compose validation and backend/frontend image building, after source jobs
     pass, using safe values from `infra/.env.example`.
6. Backend tests must use their existing Testcontainers MySQL. Do not add a
   separate GitHub Actions MySQL service.
7. npm caching may cache downloads but must not cache `node_modules`. Caches
   must improve speed without becoming correctness inputs.
8. Cancel obsolete concurrent runs for the same branch or pull request.
9. Give every job a finite, sensible timeout.
10. Do not start the complete Compose environment, publish images, or deploy in
    this ticket.
11. Update root/setup documentation with when CI runs, required jobs, commands,
    and the distinction between source tests and image builds.
12. Do not add a status badge until a real GitHub run succeeds and its URL is
    verified.

### Acceptance criteria

- Pull requests, pushes to `main`, and manual dispatch start the workflow.
- Backend verification passes, including MySQL Testcontainers integration.
- Frontend formatting, Vitest tests, and production build pass.
- Compose resolves with the example environment and both application images
  build successfully.
- A backend test failure, frontend test failure, or invalid Compose definition
  fails its relevant job/workflow.
- Workflow permissions are read-only, action references are immutable, and no
  production secret or application credential is present.
- Superseded runs are cancelled.
- Job names and logs make failures diagnosable, and each job has a timeout.
- Documentation matches the implemented workflow.
- One successful GitHub Actions run is captured as final-review evidence.

### Explicit non-goals

- Staging or production deployment
- Publishing images or artifacts
- GitHub environment or branch-protection administration
- Dependency-update automation
- Coverage thresholds
- Self-hosted runners
- A full Compose runtime/lifecycle test in CI
- Reusable-workflow or custom wrapper-script abstractions

### Implementation progress

- No R0-007 application, workflow, or documentation changes exist.
- No `.github/workflows` directory exists.
- No GitHub Actions run exists to verify.
- Whether GitHub Actions is enabled, which branch-protection rules exist, and
  which hosted-runner limits apply could not be verified from the local
  checkout.
- Exact action releases and their immutable SHAs have not been selected or
  approved.
- The only new file after this handoff operation is this handoff document; it is
  not an R0-007 implementation file.

## 7. Pending Tasks and Next Priorities

Resume R0-007 in this order:

1. Read this handoff, ADR-0002, `docs/setup/toolchain.md`, component READMEs,
   `infra/README.md`, and the current build manifests.
2. Reconfirm that `main` contains `d3c4502` or a descendant containing only this
   handoff, then create a focused R0-007 branch if that is the chosen workflow.
3. Select current official setup actions and verify the full commit SHA for each
   selected release. This is pending implementation judgment; do not guess SHA
   values.
4. Implement a small, readable CI workflow with backend and frontend jobs
   running independently and a container job dependent on them.
5. Use the commands and security boundaries in section 6 exactly; avoid an
   additional build orchestration framework.
6. Update only the documentation needed to describe CI.
7. Run all local checks listed in section 11.
8. Push the R0-007 branch, observe a real GitHub Actions run, and retain the run
   URL or captured job summary as review evidence.
9. Submit the implementation for Technical Lead review. Do not begin R1 or
   future R0 work during that review.

After R0-007 passes, the Technical Lead should decide explicitly whether R0 has
additional foundation tickets or is ready for release closure. No next ticket
beyond R0-007 has been refined or approved in the available conversation.

## 8. Architectural Decisions and Conventions

### Accepted ADRs

- **ADR-0001 — Modular monolith and monorepo:** one Angular frontend, one Spring
  Boot deployable, one MySQL ownership boundary; microservice extraction needs
  a later architecture review and ADR.
- **ADR-0002 — Repository-controlled build tooling:** Maven Wrapper and local npm
  dependencies/scripts are the build contract; the lockfile is committed; no
  extra root build system merely wraps Maven and npm.
- **ADR-0003 — Flyway-managed database migrations:** forward-only reviewed SQL,
  monotonically increasing integer versions, applied shared migrations are
  immutable, and Hibernate does not own schema creation.
- **ADR-0004 — MySQL 8.4 LTS baseline:** MySQL 8.4 is the supported server line;
  tests and local Compose currently pin patch 8.4.11.

### Engineering conventions

- Backend packages should follow business capabilities when they appear.
- Avoid generic shared-domain packages until stable reuse is demonstrated.
- Keep transactions local to the monolith unless an ADR changes the model.
- Use external configuration for secrets. Never put credentials in source,
  images, browser assets, logs, or JDBC URLs.
- Production migrations start at `V1`; `V0` is test-only.
- Runtime containers use non-root users. Local host ports bind to `127.0.0.1`;
  MySQL is not published to the host.
- Readiness represents ability to serve database-backed work; liveness must not
  fail merely because MySQL is unavailable.
- Frontend code should be organized by user-facing feature and keep strict
  TypeScript/template checks enabled.
- Use JUnit with Spring Test/MockMvc and Testcontainers for backend integration;
  use Vitest with Angular TestBed for frontend unit/integration tests.
- Architecture decisions with significant long-term cost need ADRs; routine CI
  implementation does not currently need one.

## 9. Known Issues and Risks

1. **CI is absent.** This is the current R0-007 gap. All validation is currently
   developer-driven.
2. **Docker is mandatory for backend tests.** A new laptop with an unavailable
   daemon or insufficient socket permissions will fail all Spring context tests
   that depend on Testcontainers.
3. **Java 25 warnings:** Testcontainers/JNA reports a native-access warning and
   Mockito reports dynamic agent attachment warnings. They are non-failing now
   but may require explicit test-JVM configuration under a future JDK. Do not
   suppress or skip tests to hide them.
4. **Accepted CRLF deviation:** `infra/docker-compose.yaml` is committed with
   CRLF line endings. It was reported during R0-006 final review and explicitly
   accepted by the user as non-blocking. Do not reopen it as an R0-007 blocker
   unless it causes a real CI/tooling failure.
5. **Historical review wording is stale:** R0-005 and R0-006 review records say
   approval applied to an uncommitted working tree, but both changes are now
   committed. The R0-006 record also states `git diff --check` passed even
   though its later accepted CRLF deviation was found. Treat these as historical
   evidence notes, not the current Git state.
6. **Local state is not portable:** `infra/.env`, Compose containers, images,
   and the MySQL named volume are not in Git. The destination laptop starts with
   new local database state unless it is separately and securely migrated.
7. **No production operations yet:** staging/production deployment, TLS, secret
   storage, credential separation, backup/restore, monitoring, alerting, and
   runbooks for real environments remain deferred primarily to R4.
8. **No business functionality or authentication yet:** the deny-all R0 security
   configuration is temporary. Do not infer an authentication/token design
   before R1 refinement.
9. **No browser E2E suite:** frontend routing is tested through jsdom/TestBed,
   not a real browser.
10. **Package-name context mismatch:** an earlier conversation message requested
    Java package `com.practice.retailmanagement`, while the committed code,
    documentation, and accepted R0-003 result use
    `com.retail.retailmanagement`. R0-003 was explicitly marked complete after
    that discussion. Do not rename packages automatically; request an explicit
    decision if the subject is reopened.
11. **Action SHA selection is pending:** R0-007 requires immutable action pins,
    but no versions or SHAs have been approved. Verify them against official
    action releases during implementation.

## 10. Git State and Synchronization Requirements

### Snapshot before creating this handoff

- Current branch: `main`
- Upstream: `origin/main`
- HEAD: `d3c4502 R0-006 — Establish the local Docker Compose environment.`
- Ahead/behind: `0/0`
- Existing local branches: only `main`
- Working tree: clean
- Untracked files: none
- Local commits not pushed: none
- Remote `origin`: configured for the Retail Management repository; the
  account-specific URL is intentionally not embedded in a committed document.

### State created by this handoff

`docs/handoff/PROJECT_HANDOFF.md` is a new, uncommitted file. No existing
application or configuration file was modified. Generated `backend/target` and
`frontend/dist` output from verification is ignored by Git.

The handoff will not reach the destination laptop through Git until the user
reviews, commits, and pushes it. On the source laptop:

```bash
git status --short --branch
git add docs/handoff/PROJECT_HANDOFF.md
git diff --cached --check
git diff --cached -- docs/handoff/PROJECT_HANDOFF.md
git commit -m "docs: add project handoff"
git push origin main
```

These commands are instructions only; the Codex session did not commit or push.
If direct pushes to `main` are disallowed, commit the document on a documentation
branch and merge it using the repository's normal review process. Alternatively,
copy this single document securely outside Git. Never transfer `infra/.env`
through the repository.

## 11. Commands to Build, Test, and Run the Application

Run commands from the repository root unless a subshell changes directory.

### Preflight

```bash
git status --short --branch
java --version
node --version
npm --version
docker --version
docker compose version
docker info --format 'server={{.ServerVersion}} driver={{.Driver}} os={{.OperatingSystem}}'
(cd backend && ./mvnw --version)
```

Expected major/exact versions are documented in section 4.

### Backend verification

```bash
(cd backend && ./mvnw --batch-mode --no-transfer-progress clean verify)
```

Docker must be running because this starts a disposable MySQL 8.4.11 container.

To inspect that test-only content is absent from the executable artifact:

```bash
(cd backend && jar tf target/retail-management-0.0.1-SNAPSHOT.jar | rg 'V0__|MySqlTestConfiguration|PersistenceFoundationTests')
```

The archive search should print nothing and exit with status 1 because there are
no matches.

### Frontend installation and verification

```bash
(cd frontend && npm ci)
(cd frontend && npm run verify)
```

For local development:

```bash
(cd frontend && npm start)
```

The development server is available at `http://localhost:4200/`.

### Local Compose environment

Create destination-machine local configuration once:

```bash
cp infra/.env.example infra/.env
```

Edit `infra/.env` and replace both password placeholders with different unique
local passwords. Then validate and start:

```bash
docker compose -f infra/docker-compose.yaml config --quiet
docker compose -f infra/docker-compose.yaml up --build --wait --wait-timeout 120
docker compose -f infra/docker-compose.yaml ps
```

Smoke checks:

```bash
curl --connect-timeout 2 --max-time 5 --fail http://127.0.0.1:8080/actuator/health/liveness
curl --connect-timeout 2 --max-time 5 --fail http://127.0.0.1:8080/actuator/health/readiness
curl --connect-timeout 2 --max-time 5 --fail http://127.0.0.1:4200/
curl --connect-timeout 2 --max-time 5 --fail http://127.0.0.1:4200/a-route-that-does-not-exist
curl --connect-timeout 2 --max-time 5 --output /dev/null --write-out '%{http_code}\n' http://127.0.0.1:4200/missing.js
```

The last command should print `404`. Use `infra/README.md` for the complete,
bounded outage, recovery, persistence, log, and reset procedures.

Normal shutdown preserves MySQL data:

```bash
docker compose -f infra/docker-compose.yaml down
```

Do not use `down -v` unless intentionally deleting the local MySQL volume.

### Current R0-007 candidate checks

The future CI implementation should invoke the same source commands plus:

```bash
docker compose --env-file infra/.env.example -f infra/docker-compose.yaml config --quiet
docker compose --env-file infra/.env.example -f infra/docker-compose.yaml build
```

These commands are requirements for the ticket, not evidence that CI exists.

## 12. Instructions for Resuming Work on Another Laptop

### Before leaving the source laptop

1. Review and transfer this handoff using the commands in section 10.
2. Confirm the push completed and the branch is synchronized:

   ```bash
   git status --short --branch
   git rev-list --left-right --count HEAD...@{upstream}
   ```

   Expected output after the handoff commit is a clean status and `0  0`.
3. Do not commit or copy `infra/.env`, dependency directories, build output,
   container images, or the local MySQL volume.

### On the destination laptop

Replace `<repository-url>` with the repository URL obtained from the hosting UI
or the source laptop's `git remote get-url origin` output:

```bash
git clone <repository-url> retail-management
cd retail-management
git switch main
git pull --ff-only
git status --short --branch
git log -3 --oneline --decorate
git merge-base --is-ancestor d3c4502 HEAD
```

The last command must succeed. HEAD may be newer than `d3c4502` if the handoff
document was committed after the R0-006 baseline.

Install or activate Java 25, Node 22.23.2, npm 10.9.8, Docker Engine, and the
Docker Compose plugin. Then verify and restore only non-secret dependencies:

```bash
java --version
node --version
npm --version
docker --version
docker compose version
docker info
(cd backend && ./mvnw --version)
(cd frontend && npm ci)
```

Create a new local Compose environment file and choose new passwords:

```bash
cp infra/.env.example infra/.env
```

Run the full baseline verification:

```bash
(cd backend && ./mvnw --batch-mode --no-transfer-progress clean verify)
(cd frontend && npm run verify)
docker compose --env-file infra/.env.example -f infra/docker-compose.yaml config --quiet
```

If using a feature branch for the current ticket:

```bash
git switch -c r0-007-ci-foundation
```

Start the new Codex session with:

> Read `docs/handoff/PROJECT_HANDOFF.md`, inspect the repository and ADRs, and
> continue R0-007. Do not implement another ticket. Let me implement the ticket
> unless I explicitly ask for code changes.

The exact next engineering task is R0-007 as specified in section 6. There are no
partially implemented R0-007 files to recover, merge, or preserve.

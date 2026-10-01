# Database setup

R0-004 establishes the database integration described here. Its
[final review](../reviews/R0-004.md) records the acceptance evidence.

## Runtime configuration

Provide a reachable MySQL 8.4 server, an existing application database, and a
non-root account authorized for that database. Database/account provisioning is
separate from Flyway's table migrations. For the supported local Compose
workflow, see the [infrastructure guide](../../infra/README.md).

Compose supplies these variables to the backend from its ignored `infra/.env`
file. It maps the database service to `jdbc:mysql://mysql:3306/${MYSQL_DATABASE}`;
the name `mysql` resolves only within the Compose network. The database port is
not published to the host. Create the local file from
[`infra/.env.example`](../../infra/.env.example), never commit it, and do not
place a password in a JDBC URL.

The infrastructure guide also defines the bounded local startup check and the
manual database-outage, recovery, and persistence verification procedure. Run
those checks only against a local environment that is not actively being used.

Supply these standard Spring Boot environment variables:

| Variable | Meaning |
|---|---|
| `SPRING_DATASOURCE_URL` | JDBC URL, for example `jdbc:mysql://localhost:3306/retail_management` for a workstation database |
| `SPRING_DATASOURCE_USERNAME` | Non-root application database account |
| `SPRING_DATASOURCE_PASSWORD` | Password supplied outside source control |

For Bash, after providing the URL and username, read the password without placing
its value in shell history, then run from `backend/`:

```bash
read -r -s -p 'Database password: ' SPRING_DATASOURCE_PASSWORD
export SPRING_DATASOURCE_PASSWORD
./mvnw spring-boot:run
unset SPRING_DATASOURCE_PASSWORD
```

The URL and username must also be exported in the shell or supplied through your
runtime configuration system. Spring Boot does not automatically load a project
`.env` file. Never put credentials into JDBC URLs. Connection security for shared
environments must be configured for that environment; do not copy local TLS
exceptions into staging or production.

One non-root runtime/migration account is allowed for local development by
ADR-0003. Review production credential separation in R4. With no production
migrations or entities yet, a fresh application database has no business tables.
No production V1 migration is required for this ticket.

## Integration tests

From `backend/`, run `./mvnw clean verify`. Docker must be running and accessible.
Testcontainers creates a disposable `mysql:8.4.11` container with a dynamically
assigned port and a generated password. Tests do not use Compose or a developer's
persistent database. Spring's `@ServiceConnection` supplies the test connection
details. Container lifecycle follows the Spring test context.

Test resources contribute `V0__create_persistence_smoke_table.sql`. Assertions
check successful migration history and a JDBC write/read round trip. The test
rolls back its DML so later tests do not inherit the inserted row. Future business
migrations start at V1 and also run from the main resources on fresh test databases.

After a successful verification, check the executable artifact:

```bash
jar tf target/retail-management-0.0.1-SNAPSHOT.jar | rg 'V0__|MySqlTestConfiguration|PersistenceFoundationTests'
```

Expect no matches (the search returns exit status 1 for no matches). The production
JAR must not contain test SQL, tests, or Testcontainers dependencies.

## Failure handling

- Docker socket permission or connection errors: run `docker info`, start the
  daemon if necessary, and verify the configured Docker context and authorized
  access. Do not weaken socket permissions or skip tests to obtain a green build.
- Image pull failures: check registry connectivity and the pinned image's
  availability. Do not silently change to a floating tag.
- Runtime connection refused or access denied: verify the external datasource
  settings, server availability, database existence, and account grants.
- Flyway validation failure: identify which migration differs and where it has
  been applied. Use a forward migration for shared history; do not automatically
  clean, repair, or delete a persistent database.
- A readiness-membership or Open EntityManager in View failure indicates that a
  required application setting has regressed.

Startup with missing configuration and invalid credentials was checked during
the R0-004 review. Repeat these checks after changing datasource initialization.
Diagnostic logs should identify the connection problem without exposing the
supplied password.

## Health contract

Only GET liveness and readiness are public; components and details remain hidden.
The intended readiness group includes `readinessState,db`; liveness includes
`livenessState` only. Database downtime after startup should make readiness return
HTTP 503 while liveness stays HTTP 200. This prevents database outages from
causing application restart storms. Health-group membership is covered by tests,
and the R0-004 review verified the behavior against a stopped disposable MySQL
instance.

## References

- [Spring Boot Testcontainers](https://docs.spring.io/spring-boot/reference/testing/testcontainers.html)
- [Spring Boot health groups and probes](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html)
- [ADR-0003](../adr/0003-flyway-managed-database-migrations.md)
- [ADR-0004](../adr/0004-mysql-8-4-lts-baseline.md)

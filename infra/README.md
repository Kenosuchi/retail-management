# Infrastructure

R0-006 provides a Docker Compose environment for local integration of the
Angular frontend, Spring Boot backend, and MySQL database. It is a developer
environment, not a staging or production deployment definition.

## What Compose starts

| Service | Purpose | Host access |
|---|---|---|
| `frontend` | Serves the production Angular build through Nginx | `http://127.0.0.1:4200` |
| `backend` | Runs the Spring Boot modular monolith | `http://127.0.0.1:8080` |
| `mysql` | Stores local application data | Internal Compose network only |

The backend connects to MySQL at `mysql:3306`, where `mysql` is the Compose
service name. It is not `localhost`: inside a container, `localhost` means that
same container. MySQL is intentionally not published to the host.

The `mysql-data` named volume persists database files across normal container
stops and recreation. A container is disposable; a named volume is the local
state that must be treated carefully.

## First-time setup

From the repository root, create your local environment file:

```bash
cp infra/.env.example infra/.env
```

Replace every password placeholder in `infra/.env` with a unique local value.
`infra/.env` is ignored by Git and must never be committed. The committed
example lists variable names and safe placeholders only.

Verify that Compose can resolve the environment and syntax before building:

```bash
docker compose -f infra/docker-compose.yaml config --quiet
```

## Start and inspect the environment

Run these commands from the repository root:

```bash
docker compose -f infra/docker-compose.yaml up --build --wait --wait-timeout 120
docker compose -f infra/docker-compose.yaml ps
```

Compose waits for all three services to become healthy, for up to 120 seconds
after starting them. The backend starts after MySQL becomes healthy. The static
frontend starts independently of the backend. Startup ordering does not
guarantee that a dependency stays available later.

Follow a service's standard-output logs with:

```bash
docker compose -f infra/docker-compose.yaml logs -f backend
docker compose -f infra/docker-compose.yaml logs -f frontend
docker compose -f infra/docker-compose.yaml logs -f mysql
```

## Startup smoke check

The `up --wait --wait-timeout 120` command above fails if any service does not
become healthy within its startup limit. After it succeeds, check the published
HTTP paths. Each request has its own five-second limit:

```bash
curl --connect-timeout 2 --max-time 5 --fail http://127.0.0.1:8080/actuator/health/liveness
curl --connect-timeout 2 --max-time 5 --fail http://127.0.0.1:8080/actuator/health/readiness
curl --connect-timeout 2 --max-time 5 --fail http://127.0.0.1:4200/
curl --connect-timeout 2 --max-time 5 --fail http://127.0.0.1:4200/a-route-that-does-not-exist
curl --connect-timeout 2 --max-time 5 --output /dev/null --write-out '%{http_code}\n' http://127.0.0.1:4200/missing.js
```

The health responses contain only aggregate status, such as `{"status":"UP"}`.
The unknown frontend route returns the Angular application shell, after which
Angular renders its **Page not found** route in a browser. The final command
must print `404`, proving that missing JavaScript assets do not incorrectly
receive the single-page-application fallback.

For a browser check, open `http://127.0.0.1:4200/`, then open or refresh an
unknown URL such as `http://127.0.0.1:4200/unknown`. Confirm the application
shows **Page not found** and that its recovery link returns to the home page.

## Database outage, recovery, and persistence checks

Run these checks only when no one is using this local environment. Stopping the
database temporarily makes the backend unavailable for database-backed work.
It does not delete the named volume.

First, complete the startup smoke check. Run this entire Bash block together.
It records when the backend container started, stops MySQL, checks the health
responses, and waits for recovery. Its exit trap attempts to start MySQL even
if a check fails. Do not use `curl --fail` during the outage: HTTP `503` is
the expected readiness response.

```bash
(
  set -e
  compose=(docker compose -f infra/docker-compose.yaml)
  backend_id="$("${compose[@]}" ps -q backend)"
  test -n "$backend_id"
  backend_started_before="$(docker inspect --format '{{.State.StartedAt}}' "$backend_id")"
  test -n "$backend_started_before"

  trap 'docker compose -f infra/docker-compose.yaml start mysql' EXIT
  "${compose[@]}" stop mysql

  outage_seen=0
  for attempt in $(seq 1 4); do
    liveness_status="$(curl --connect-timeout 2 --max-time 5 --silent --output /dev/null --write-out '%{http_code}' http://127.0.0.1:8080/actuator/health/liveness || true)"
    readiness_status="$(curl --connect-timeout 2 --max-time 40 --silent --output /dev/null --write-out '%{http_code}' http://127.0.0.1:8080/actuator/health/readiness || true)"

    printf 'liveness=%s readiness=%s\n' "$liveness_status" "$readiness_status"
    if [ "$liveness_status" = 200 ] && [ "$readiness_status" = 503 ]; then
      outage_seen=1
      break
    fi
    sleep 2
  done

  test "$outage_seen" -eq 1 || {
    echo 'Timed out waiting for liveness=200 and readiness=503.' >&2
    exit 1
  }

  "${compose[@]}" start mysql
  "${compose[@]}" up --no-recreate --wait --wait-timeout 120
  backend_started_after="$(docker inspect --format '{{.State.StartedAt}}' "$backend_id")"
  test -n "$backend_started_after"
  printf 'backend started before=%s after=%s\n' "$backend_started_before" "$backend_started_after"
  test "$backend_started_before" = "$backend_started_after"
  curl --connect-timeout 2 --max-time 5 --fail http://127.0.0.1:8080/actuator/health/readiness
  trap - EXIT
)
```

Expect `liveness=200 readiness=503` during the outage, followed by successful
readiness and identical backend start times after MySQL recovers. The database
health request can take longer than a normal HTTP request while its connection
attempt expires, so this check allows up to 40 seconds per readiness request
and at most four attempts. A failed read or wait exits with a failure status.
If the block fails, inspect the logs and confirm MySQL is running before trying
again.

MySQL stores a generated server UUID in `auto.cnf` inside its data directory.
Compare that UUID before and after normal container recreation to verify that
Compose reused the same MySQL data directory. Run this entire Bash block
together. It creates and deletes no application tables and requires no database
password:

```bash
(
  set -e
  compose=(docker compose -f infra/docker-compose.yaml)
  mysql_uuid_before="$("${compose[@]}" exec -T mysql sh -c 'sed -n "s/^server-uuid=//p" /var/lib/mysql/auto.cnf')"
  test -n "$mysql_uuid_before"

  "${compose[@]}" down
  "${compose[@]}" up --wait --wait-timeout 120

  mysql_uuid_after="$("${compose[@]}" exec -T mysql sh -c 'sed -n "s/^server-uuid=//p" /var/lib/mysql/auto.cnf')"
  test -n "$mysql_uuid_after"
  printf 'MySQL UUID before=%s after=%s\n' "$mysql_uuid_before" "$mysql_uuid_after"
  test "$mysql_uuid_before" = "$mysql_uuid_after"
)
```

The final `test` must succeed. Record both UUID values as acceptance evidence.
This proves the MySQL-managed data directory persisted; it does not test a
business row, because R0 has no production business tables. MySQL documents
that it reads this UUID from the data directory at startup and generates a new
one only if the file is absent.

See the [MySQL 8.4 server UUID documentation](https://dev.mysql.com/doc/refman/8.4/en/replication-options.html).

## Stop, restart, and reset

A normal stop preserves MySQL data:

```bash
docker compose -f infra/docker-compose.yaml down
```

Starting the environment again reuses `mysql-data`:

```bash
docker compose -f infra/docker-compose.yaml up -d
```

Use the following command only when you deliberately want to delete all local
Compose volumes, including MySQL data:

```bash
docker compose -f infra/docker-compose.yaml down -v
```

`down -v` is destructive and cannot be undone. It is not part of normal
shutdown, troubleshooting, or automated verification.

## Verification boundary

Building an image packages an application; it does not replace the repository
test suites. Before considering a Compose change verified, run the component
checks independently:

```bash
(cd backend && ./mvnw clean verify)
(cd frontend && npm ci && npm run verify)
```

Backend integration tests use an isolated Testcontainers MySQL instance, not
this persistent Compose database. Do not point tests at `mysql-data` or add the
Docker socket to application containers just to run tests.

## Security notes and scope

The published frontend and backend ports bind to `127.0.0.1`, limiting them to
the local machine. Secrets are supplied to Compose from the ignored local
environment file and must not be placed in images, source code, browser assets,
or logs.

This definition does not select a production secret store, TLS termination,
backup strategy, deployment platform, Redis, Kafka, Kubernetes, or a
microservice architecture. Those require their own requirements and release
scope.

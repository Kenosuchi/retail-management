# ADR-0003: Flyway-managed database migrations

**Status:** Accepted

**Date:** 2026-09-10

## Context

MySQL is the platform's primary source of truth. Schema changes must be reproducible on empty databases, reviewable alongside application changes, and safely promotable through shared environments.

Hibernate understands entity mappings but does not provide a sufficient audited deployment history for production schema evolution. Manually executed SQL would make environment state depend on operator memory and timing.

## Decision

Flyway will be the exclusive owner of database schema evolution. Production migrations will be SQL files committed with the backend and written for the supported MySQL version.

Versioned migrations will use monotonically increasing integer versions and descriptive names following `V<version>__<description>.sql`. Once a migration has reached a shared environment, it is immutable. Corrections use a new forward migration rather than editing deployed history.

Hibernate will validate its mappings against the migrated schema. Automatic schema creation or update is not allowed in production-like configurations.

Repeatable migrations are reserved for database objects whose complete definition is intentionally reapplied, such as a view or stored object. They are not a substitute for versioned table evolution.

Local development may initially use one non-root database account for application access and migrations. Separating production migration credentials from runtime credentials is required to be reviewed during production hardening in R4.

## Alternatives considered

### Liquibase

Liquibase provides structured change types, metadata, and support for database-independent descriptions. It was not selected because the platform has one known database, and SQL-first migrations make MySQL behavior and review impact explicit with less abstraction.

### Hibernate automatic DDL

Hibernate can create or update a schema from entity mappings and is convenient for prototypes. It was rejected for production-like use because generated changes are not a reviewed deployment plan, destructive behavior can be surprising, and database objects outside entity mappings are poorly represented.

### Manually executed SQL scripts

Manual execution permits direct control over SQL. It was rejected because it does not reliably record ordering, checksums, or which environments received each change.

### Rollback script for every migration

Paired down scripts can appear to offer easy rollback. They were not required because destructive schema operations and data transformations are not always reversible, and MySQL DDL must not be assumed to participate fully in application-style transactions. Recovery may require a corrective migration or restoration from a tested backup.

## Consequences

### Positive

- Schema history is versioned and reviewed with application code.
- Fresh and existing environments follow the same ordered migration path.
- SQL behavior remains visible to engineers working with MySQL.
- Drift caused by Hibernate mutation or manual operator steps is reduced.

### Negative

- Engineers must understand MySQL DDL, locking, and migration safety.
- Forward-only corrections can require more planning than editing a script locally.
- Parallel branches can choose the same integer version and require renumbering before merge.
- Large data migrations may require operational procedures beyond a normal startup migration.

## Risks

- A long-running or locking migration can disrupt application availability. Potentially expensive changes require execution-plan and operational review.
- Editing an applied migration causes checksum divergence. Shared migration history must remain immutable.
- Placing seed credentials or sensitive production data in migrations would expose them through source control. Migrations must contain schema changes and approved non-secret reference data only.
- Giving the runtime account permanent DDL privileges increases impact if the application is compromised. Production credential separation is deferred but explicitly required for R4 review.

## Revisit triggers

- The platform must support multiple database engines with the same change definitions.
- Migration volume or coordination makes integer version conflicts materially costly.
- Zero-downtime schema changes require a dedicated migration service or operational workflow.
- Governance requirements mandate a different approval, provenance, or rollback mechanism.

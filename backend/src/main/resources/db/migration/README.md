# Production database migrations

Flyway owns schema evolution under
[ADR-0003](../../../../../../docs/adr/0003-flyway-managed-database-migrations.md).

Production SQL migrations start at `V1` when the first real domain schema is
introduced. Use monotonically increasing integer versions and descriptive names:
`V<version>__<description>.sql`. Do not add a no-op migration just to occupy V1.

Applied shared migrations are immutable. Correct them with a new forward
migration. Never hide validation failures by casually running repair, enabling
baseline-on-migrate, or editing history. MySQL DDL must not be assumed to roll
back like application DML transactions.

`V0__create_persistence_smoke_table.sql` lives in test resources only. It creates
a disposable fixture and must never enter a production JAR or shared database.
Only future production `V1` and later scripts belong in this directory.

Migrations contain no credentials or sensitive data. Hibernate validates schema
mappings; it does not create or update tables. The README is not a SQL migration.

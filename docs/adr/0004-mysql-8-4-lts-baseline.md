# ADR-0004: MySQL 8.4 LTS baseline

**Status:** Accepted

**Date:** 2026-09-13

## Context

MySQL is already the primary source of truth under ADR-0001. Flyway migrations,
the JDBC driver, and future entity mappings need one explicit server baseline.
R0 introduces no requirement for features exclusive to newer server lines.

## Decision

Use MySQL 8.4 LTS as the initial supported server line. Integration tests use
an explicitly pinned patch image, initially `mysql:8.4.11`. R0-006 must align
local Compose with this baseline; later staging and production must use a
reviewed compatible patch. Patch updates require release-note review and backend
verification. The patch pin is maintained in configuration, not fixed forever
by this ADR. Tags are not immutable; digest pinning can be added with the image
provenance policy during production hardening.

Spring Boot dependency management supplies Connector/J, Hibernate, and Flyway
versions. Real MySQL tests verify integration; selecting a BOM does not prove
every future schema or query is compatible.

Technical Lead review accepts the server-line choice proposed and verified in
R0-004.

## Alternatives considered

### MySQL 9.7 LTS

A reasonable newer LTS baseline with a later lifecycle. No current requirement
needs its additional features. Choose 8.4 for this foundation and review a
major-line upgrade when features, support requirements, or deployment constraints
justify it. This is not a claim that 9.7 is unsuitable for production.

### MySQL Innovation releases

Provide newer functionality with more frequent behavior changes and upgrade
work. That maintenance cadence has no demonstrated benefit for this platform.

## Consequences

### Positive

- Tests and future environments have an explicit MySQL compatibility target.
- LTS reduces feature and behavior churn within the selected line.
- Patch selection is reviewable without creating a new ADR for every update.

### Negative

- The team owns patch monitoring and eventual major-line upgrades.
- Features exclusive to newer lines cannot be assumed in migrations or queries.

## Risks

- A stale patch pin can retain defects; inspect updates regularly.
- Passing foundation tests does not establish future SQL compatibility or
  application performance. Domain migrations and queries need their own tests.
- An image registry or platform restriction may require revisiting distribution.

## Revisit triggers

- A required capability depends on a newer server line.
- Deployment platform support or MySQL lifecycle constraints change.
- A measured compatibility, security, or operational issue warrants an upgrade.

## Reference

[MySQL release model](https://dev.mysql.com/doc/refman/8.4/en/mysql-releases.html)
describes the LTS and Innovation tracks and their upgrade considerations.

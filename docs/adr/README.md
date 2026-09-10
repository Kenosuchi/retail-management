# Architecture decision records

Architecture decision records capture consequential decisions, the context in which they were made, alternatives considered, and known consequences. They are historical records. The [current architecture document](../architecture/README.md) remains the concise description of how the system is intended to work now.

## Status lifecycle

- **Proposed:** Ready for technical review but not yet binding.
- **Accepted:** Reviewed and currently binding.
- **Rejected:** Considered but not selected.
- **Deprecated:** Retained for history but no longer recommended.
- **Superseded:** Replaced by a later ADR, which must be linked.

An author submits a consequential decision as `Proposed`. The Technical Lead accepts or rejects it after review. An accepted ADR is not rewritten to make a materially different decision; create a new ADR and mark the old one `Superseded`. Small corrections that do not change meaning are allowed.

## Naming and numbering

- Use four-digit, monotonically increasing identifiers.
- Use lowercase kebab-case filenames: `NNNN-short-decision-title.md`.
- Never reuse an ADR number, including after rejection.
- Dates use ISO `YYYY-MM-DD` format.

Start from [ADR-0000](0000-template.md). Create an ADR when a decision meaningfully affects system structure, data ownership, security, deployment, operational responsibility, or the cost of later change. Routine implementation details do not require an ADR.

## Decision index

| ADR | Status | Decision |
|---|---|---|
| [0001](0001-modular-monolith-and-monorepo.md) | Accepted | Start with a modular monolith in a monorepo |
| [0002](0002-repository-controlled-build-tooling.md) | Accepted | Use repository-controlled Maven and npm tooling |
| [0003](0003-flyway-managed-database-migrations.md) | Accepted | Make Flyway the owner of schema evolution |

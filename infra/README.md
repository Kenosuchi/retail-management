# Infrastructure

This directory will contain version-controlled infrastructure used to run the platform locally and, in later releases, deploy it.

Expected responsibilities include:

- Dockerfiles and Docker Compose definitions;
- local MySQL configuration;
- deployment configuration when a target environment is selected;
- infrastructure-specific operational documentation.

Infrastructure definitions must not contain production credentials. Runtime secrets belong in an environment-appropriate secret store and are supplied through external configuration.

No infrastructure is introduced by R0-002. Docker composition begins in R0-006, and production deployment work belongs to R4.

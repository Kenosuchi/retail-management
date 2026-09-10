# Backend

This directory will contain the Java 25 and Spring Boot 4.1 modular-monolith application.

## Ownership

The backend will own:

- HTTP application APIs;
- authentication and authorization enforcement;
- business capabilities and transaction boundaries;
- persistence through the platform's single MySQL schema;
- database migrations;
- server-side health, logs, metrics, and traces.

Business code will be organized by capability rather than by global technical layers. A capability may use another capability only through an explicit module-facing application interface; it must not reach into another capability's entities, repositories, or other internal implementation types.

No backend application has been generated yet. That work starts in R0-003.

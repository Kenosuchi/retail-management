# Frontend

This directory will contain the Angular 21 browser application.

## Ownership

The frontend will own:

- browser rendering and interaction;
- client-side navigation and presentation state;
- communication with the backend through documented HTTP APIs;
- browser-focused accessibility and automated tests.

Features will be organized by user-facing capability as they are introduced. Shared code must have demonstrated reuse and a stable meaning; `shared` must not become a destination for unrelated code.

Browser-delivered configuration is public and must never contain credentials or other secrets. Deployment-specific API hosts must not be hardcoded into feature code.

No frontend application has been generated yet. That work starts in R0-005.

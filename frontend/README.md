# Retail Management frontend

This directory contains the Angular 21 browser application for the Retail
Management Platform. The Angular CLI and all build dependencies are installed
locally and invoked through npm scripts; a global Angular CLI is not part of the
project toolchain.

## Current scope

The R0 frontend is a standalone, strict, zoneless Angular application. It
provides the application shell, a foundation page at `/`, and a not-found page
for unknown routes. Business features, backend API integration, authentication,
and end-to-end testing are not part of the current implementation.

## Ownership

The frontend owns:

- browser rendering and interaction;
- client-side navigation and presentation state;
- communication with the backend through documented HTTP APIs;
- browser-focused accessibility and automated tests.

Features will be organized by user-facing capability as they are introduced.
Shared code must have demonstrated reuse and a stable meaning; `shared` must not
become a destination for unrelated code.

Browser-delivered configuration is public and must never contain credentials or
other secrets. Deployment-specific API hosts must not be hardcoded into feature
code.

## Install dependencies

From this directory, install exactly the dependency graph recorded in
`package-lock.json`:

```bash
npm ci
```

Use Node.js 22.23.2 and npm 10.9.8 as recorded by the repository toolchain
contract.

## Run locally

Start the development server with:

```bash
npm start
```

Open `http://localhost:4200/`. The server reloads the application when source
files change.

## Verify the frontend

Run the complete local verification workflow with:

```bash
npm run verify
```

This checks formatting, runs the Vitest suite once, and creates a production
build. The build output is written under `dist/frontend/`.

For a watch-mode test loop during development, run:

```bash
npm test
```

For a terminating test run without the build and formatting checks, run:

```bash
npm run test:ci
```

## Generate Angular code

Invoke the repository-local Angular CLI through the npm script. For example:

```bash
npm run ng -- generate component features/example
```

Review generated files before committing them and keep code organized around
user-facing features rather than generic technical folders.

## End-to-end tests

No end-to-end test framework or `e2e` target is configured in R0. Introduce one
only through a ticket that defines the required browser journeys and CI
execution environment.

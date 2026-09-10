# Development toolchain

This document is the workstation contract for R0. It separates project requirements from versions merely observed on one developer machine.

## Required tools

| Tool | Project requirement | Verified locally on 2026-09-10 UTC | How the project will enforce it |
|---|---|---|---|
| Git | A version capable of the documented clone and branch workflow | 2.53.0 | Repository history and documented workflow |
| Java | Java 25 LTS JDK | Eclipse Temurin 25.0.4+7 LTS | The backend build will target Java 25 in R0-003 |
| Node.js | 22.23.2 | 22.23.2 | `.nvmrc` |
| npm | Compatible with the selected Node.js runtime | 10.9.8 | The frontend manifest and lockfile will establish the package-manager contract in R0-005 |
| Docker Engine | A functioning Docker Engine accessible to the developer | Client and server 29.8.0 | Container image versions and Compose behavior will be established in R0-006 |
| Docker Compose | The `docker compose` plugin | 5.5.1 | The Compose definition and smoke workflow will be established in R0-006 |

Angular 21 supports Node.js `^20.19.0`, `^22.12.0`, or `^24.0.0`. This repository pins Node.js 22.23.2 as its tested development version rather than relying on the entire supported range.

Maven and the Angular CLI are intentionally not required as global installations. R0-003 will introduce the Maven wrapper, and R0-005 will introduce a repository-local Angular CLI invoked through npm scripts.

## Workstation setup expectations

1. Clone the intended repository instead of initializing over an unknown working tree. If a remote already exists, inspect it before creating the first commit.
2. Install a Java 25 JDK and ensure it is the active JDK in a fresh shell.
3. Use `.nvmrc` with a compatible Node version manager, or install the exact Node.js version recorded in that file.
4. Install Docker Engine or Docker Desktop with the Compose plugin and ensure the current user can reach the Docker daemon.
5. Never use a global Angular CLI or Maven installation as evidence that a repository build is reproducible.

Installation procedures vary by operating system. Use the official vendor documentation and verify the result with the commands below rather than relying only on an installer reporting success.

## Preflight verification

Run these commands from the repository root in a fresh shell:

```bash
git rev-parse --is-inside-work-tree
git status --short --branch
java --version
node --version
npm --version
docker --version
docker compose version
docker info --format 'server={{.ServerVersion}} driver={{.Driver}} os={{.OperatingSystem}}'
```

The checks pass when:

- Git prints `true` and reports the expected branch.
- Java reports major version 25.
- Node reports `v22.23.2`.
- npm returns a valid semantic version.
- Docker and Compose return versions, and `docker info` reports a server rather than a socket or permission error.

After frontend dependencies exist, use only the Angular CLI version installed in the frontend workspace through its npm scripts.

## Repository and remote verification record

At the R0-001 review on 2026-09-10 UTC:

- Git recognized this directory as a worktree.
- `origin` was configured.
- `git ls-remote --heads origin` returned no branches, confirming that the remote did not contain branch history to preserve at that time.

This is an audit record, not a permanent assumption. Check the remote again before any future repository reinitialization or history-changing operation.

## Known global Angular CLI issue

Angular CLI 21.2.19 is installed globally on the verified workstation, but `ng version` fails while parsing the npm version and reports `Invalid semver version for npm: ""`.

This global installation is not part of the project toolchain. Do not work around it by making global state a build prerequisite. R0-005 must install a compatible CLI locally and expose it through npm scripts.

## Secrets and generated files

- Never commit `.env` or environment-specific `.env.*` files. A sanitized `.env.example` may be committed as documentation.
- Never commit private keys, keystores, local secret directories, tokens, or credentials.
- Review staged changes before every commit, even when `.gitignore` is present.
- Do not commit dependency directories, compiled output, coverage output, logs, or IDE-local metadata.

Useful checks before committing are:

```bash
git status --short
git diff --cached
git status --ignored --short
```

Ignore rules reduce accidents; they are not a substitute for secret scanning or review.

# CLAUDE.md

Context for AI coding agents working in this repository.

## Project

**Pixroost** — a free, local-first app that unifies photos from the phone gallery and the user's clouds
(Yandex Disk, Dropbox, OneDrive, WebDAV, Google Photos picker/Takeout) into one feed, finds duplicates
across clouds, and sends originals to the user's own computer. Platforms: Android and Windows (+ Linux) for v1.0;
iOS and macOS come later (see docs/architecture/adr/0008-android-windows-first.md).
Stack: Kotlin Multiplatform + Compose Multiplatform.

Status: **planning** — no code yet. Design: first mockups proposed in `docs/design/` (owner review pending);
do not change the visual direction without the owner.

## Where things are

- Product, architecture, conventions: `docs/` (Russian). Start with `docs/README.md`.
- Architecture decisions: `docs/architecture/adr/`.
- Planned module layout: `docs/tech/project-structure.md`.

## Rules

- Docs in `docs/` are written in **Russian**. Code, code comments, commit messages, branch names — **English**.
  `README.md` (EN) and `README.ru.md` (RU) must be kept in sync.
- Branches: `<type>/<issue>-<short-name>` (agent branches `claude/*` are allowed).
- Commits and PR titles: Conventional Commits, e.g. `feat(devices): add QR pairing screen`.
  Types and scopes: `docs/process/conventions.md`.
- Squash merges into `main`; `main` must always build.
- Behavior or architecture change → update `docs/` in the same PR; hard-to-reverse decision → new ADR.
- Privacy: no analytics/ads SDKs, no network calls to third parties except the user's chosen cloud services.
- Anything that deletes user files must go through the cleanup safety policy
  (`docs/architecture/data-model.md`, "Правило безопасности удаления") and be covered by tests.
- Never commit secrets (keystores, `.p8`, `.env`, `local.properties`).

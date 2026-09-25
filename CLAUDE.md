# CLAUDE.md

Context for AI coding agents working in this repository.

## Project

**Pixroost** — a free, local-first app that unifies photos from the phone gallery and the user's clouds
(Yandex Disk, Dropbox, OneDrive, WebDAV, Google Photos picker/Takeout) into one feed, finds duplicates
across clouds, and sends originals to the user's own computer. Platforms: Android and Windows (+ Linux) for v1.0;
iOS and macOS come later (see docs/architecture/adr/0008-android-windows-first.md).
Stack: Kotlin Multiplatform + Compose Multiplatform.

Status: **early development** — the Gradle skeleton (step 1.1) builds; no product features yet. Design: first mockups proposed in `docs/design/` (owner review pending);
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

## Building in a cloud session

- Project JDK is **25** (Gradle toolchain). UI rule: Compose everywhere; whatever Compose can't do well
  on iOS or macOS is built in SwiftUI (`docs/architecture/adr/0009-liquid-glass-native-navigation.md`).
- The environment's network access must allow `dl.google.com` (Google Maven and the Android SDK).
  Maven Central, the Gradle Plugin Portal and `services.gradle.org` are needed as well.
- Setup script for the environment (installs JDK 25 and the Android SDK; iOS builds need macOS and run in CI):

  ```bash
  #!/usr/bin/env bash
  set -eu
  apt-get update -qq
  apt-get install -y -qq openjdk-25-jdk-headless unzip
  SDK=/opt/android-sdk
  if [ ! -x "$SDK/cmdline-tools/latest/bin/sdkmanager" ]; then
    mkdir -p "$SDK/cmdline-tools"
    curl -fsSL -o /tmp/cmdline-tools.zip \
      https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip
    unzip -q /tmp/cmdline-tools.zip -d "$SDK/cmdline-tools"
    mv "$SDK/cmdline-tools/cmdline-tools" "$SDK/cmdline-tools/latest"
  fi
  yes | "$SDK/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$SDK" --licenses > /dev/null || true
  "$SDK/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$SDK" "platform-tools"
  echo "export ANDROID_HOME=$SDK" > /etc/profile.d/android-sdk.sh
  ```

  With licenses accepted, the Android Gradle plugin downloads the platform and build tools it needs.

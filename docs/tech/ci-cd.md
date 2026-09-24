# CI/CD

GitHub Actions. Для **публичных** репозиториев минуты стандартных раннеров, включая macOS, бесплатны.
Это одна из причин держать репозиторий открытым: сборка iOS в облаке ничего не стоит.

## Что уже есть

| Workflow | Что делает |
|---|---|
| [`pr-title.yml`](../../.github/workflows/pr-title.yml) | проверяет, что заголовок PR соответствует [Conventional Commits](../process/conventions.md#2-коммиты) |

## План

### `ci.yml` — на каждый PR и push в `main`

| Job | Раннер | Шаги |
|---|---|---|
| `lint` | ubuntu | Spotless (ktlint), detekt |
| `test-shared` | ubuntu | unit-тесты `commonTest` на JVM, Kover |
| `android` | ubuntu | сборка debug APK, unit-тесты Android |
| `desktop` | ubuntu | сборка desktop, `desktopTest` |
| `ios` | macos | сборка фреймворка и приложения для симулятора, `iosTest` |
| `db` | ubuntu | проверка миграций SQLDelight |

Сборки идут параллельно, кэшируются Gradle и Kotlin/Native (`~/.konan`).

### `release-please.yml` — на push в `main`

[release-please](https://github.com/googleapis/release-please) читает коммиты Conventional Commits и держит
открытым PR «chore(release): 1.2.0» с обновлённым `CHANGELOG.md` и версией. Слияние этого PR создаёт тег
`vX.Y.Z` и GitHub Release.

### `release.yml` — на тег `v*`

| Job | Раннер | Результат |
|---|---|---|
| `android-release` | ubuntu | подписанный AAB → Google Play (внутреннее тестирование) и RuStore; APK → GitHub Release |
| `ios-release` | macos | архив → TestFlight через App Store Connect API — этап «iOS и macOS» |
| `macos-release` | macos | `.dmg`, подпись Developer ID, нотаризация, staple → GitHub Release — этап «iOS и macOS» |
| `windows-release` | windows | `.msi` → GitHub Release; MSIX → Microsoft Store |
| `linux-release` | ubuntu | `.deb` → GitHub Release (без официальной поддержки) |

Выпуск в production в сторах — **вручную** в консолях, после проверки бета-версии.

### `website.yml`

Сборка лендинга и деплой на GitHub Pages при изменениях в `website/`.

### Безопасность

- **CodeQL** (Java/Kotlin) — на PR и раз в неделю.
- **Dependabot** — Gradle и GitHub Actions, раз в неделю, группами.
- **Secret scanning** и **push protection** — включить в настройках репозитория.

## Секреты CI

Хранятся в *Settings → Secrets and variables → Actions*, никогда в репозитории.

| Секрет | Для чего |
|---|---|
| `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD` | подпись Android-сборок (или Play App Signing + upload key) |
| `PLAY_SERVICE_ACCOUNT_JSON` | загрузка в Google Play |
| `RUSTORE_API_KEY` | загрузка в RuStore |
| `ASC_KEY_ID`, `ASC_ISSUER_ID`, `ASC_PRIVATE_KEY` | App Store Connect API (TestFlight) |
| `APPLE_DEVELOPER_ID_CERT_P12`, `APPLE_DEVELOPER_ID_CERT_PASSWORD` | подпись macOS |
| `APPLE_NOTARY_*` | нотаризация |
| `MS_STORE_*` | загрузка в Microsoft Store |

Секреты появляются по мере регистрации в сторах ([distribution.md](../release/distribution.md)).

## Защита `main` (настроить вручную)

Правила для `main` (только через PR, только squash, линейная история, обязательная проверка заголовка PR,
запрет force push) настраиваются через **Rulesets** — пошагово в
[настройке репозитория](../process/repository-setup.md#5-защита-ветки-main).

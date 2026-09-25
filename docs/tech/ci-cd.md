# CI/CD

GitHub Actions. Для **публичных** репозиториев минуты стандартных раннеров, включая macOS, бесплатны.
Это одна из причин держать репозиторий открытым: сборка iOS в облаке ничего не стоит.

## Что уже есть

| Workflow | Что делает |
|---|---|
| [`ci.yml`](../../.github/workflows/ci.yml) | проверки и сборка на каждый PR и push в `main`, [подробно ниже](#ciyml--на-каждый-pr-и-push-в-main) |
| [`pr-title.yml`](../../.github/workflows/pr-title.yml) | проверяет, что заголовок PR соответствует [Conventional Commits](../process/conventions.md#2-коммиты) |
| [`labels.yml`](../../.github/workflows/labels.yml) | создаёт и обновляет лейблы из [`.github/labels.yml`](../../.github/labels.yml) |
| [`roadmap-setup.yml`](../../.github/workflows/roadmap-setup.yml) | вручную: привязывает эпики к вехам и лейблам |
| [`dependabot.yml`](../../.github/dependabot.yml) | раз в неделю обновляет GitHub Actions и зависимости Gradle: Kotlin с Compose и AndroidX — группами; AGP — только патч-версии, минорные поднимаем вручную, когда их поддерживает стабильная Android Studio |

## `ci.yml` — на каждый PR и push в `main`

| Job | Раннер | Что делает |
|---|---|---|
| `Build (ubuntu-latest)` | Linux | `./gradlew check` ([проверки кода](../process/conventions.md#проверки-кода), тесты на desktop и Android host, Android lint), debug APK, desktop-приложение; APK — артефакт сборки |
| `Build (windows-latest)` | Windows | то же на Windows: ловит проблемы путей и окончаний строк |
| `iOS` | macOS | тесты общего кода на симуляторе, фреймворк `PixroostKit`, сборка приложения из Xcode-проекта для симулятора без подписи |

- Actions закреплены по SHA коммита, версия — в комментарии; обновляет их Dependabot. Права — только чтение.
- Кэш Gradle — `gradle/actions/setup-gradle` с открытым провайдером `basic`, кэш Kotlin/Native — `~/.konan`.
  Кэш Gradle пополняют только сборки `main`, PR его только читают.
- Новый push в PR отменяет предыдущий запуск этого PR.
- Если сборка упала, отчёты (тесты, detekt, lint) лежат в артефакте `reports-<раннер>`.

### APK на телефон

Каждая сборка на Linux прикладывает `pixroost-debug.apk` отдельным файлом, без zip. На телефоне: GitHub →
**Actions** → последний запуск **CI** на `main` → *Artifacts* → `pixroost-debug.apk` → открыть и установить.
Нужен вход в GitHub; файл хранится 14 дней.

APK подписан тем же отладочным ключом, что и сборки на ПК, поэтому ставится поверх них. Ключ лежит в секрете
`ANDROID_DEBUG_KEYSTORE` ([как добавить](#секреты-ci)). Без секрета APK подписывается новым ключом, и
перед установкой старую версию придётся удалить.

### Что добавим

| Когда | Что добавить |
|---|---|
| шаг 3.5 (Room) | тесты миграций на JVM и сверка экспортированной схемы |
| Фаза 7 | `release-please.yml` и `release.yml` (ниже) |

## План

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
| `ANDROID_DEBUG_KEYSTORE` | отладочный ключ Android (`debug.keystore` в base64), чтобы APK из CI ставился поверх сборок с ПК |
| `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD` | подпись Android-сборок (или Play App Signing + upload key) |
| `PLAY_SERVICE_ACCOUNT_JSON` | загрузка в Google Play |
| `RUSTORE_API_KEY` | загрузка в RuStore |
| `ASC_KEY_ID`, `ASC_ISSUER_ID`, `ASC_PRIVATE_KEY` | App Store Connect API (TestFlight) |
| `APPLE_DEVELOPER_ID_CERT_P12`, `APPLE_DEVELOPER_ID_CERT_PASSWORD` | подпись macOS |
| `APPLE_NOTARY_*` | нотаризация |
| `MS_STORE_*` | загрузка в Microsoft Store |

Секреты появляются по мере регистрации в сторах ([distribution.md](../release/distribution.md)).

Как добавить `ANDROID_DEBUG_KEYSTORE`: отладочный ключ Android Studio создаёт сама при первой сборке. В PowerShell
скопировать его в буфер обмена:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("$env:USERPROFILE\.android\debug.keystore")) | Set-Clipboard
```

На Mac: `base64 -i ~/.android/debug.keystore | pbcopy`. Затем *Settings → Secrets and variables → Actions →
New repository secret*: имя `ANDROID_DEBUG_KEYSTORE`, значение — из буфера обмена.

## Защита `main` (настроить вручную)

Правила для `main` (только через PR, только squash, линейная история, обязательная проверка заголовка PR,
запрет force push) настраиваются через **Rulesets** — пошагово в
[настройке репозитория](../process/repository-setup.md#5-защита-ветки-main).

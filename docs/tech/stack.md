# Технологический стек

Версии — актуальные на сентябрь 2026. Точные номера фиксируются в `gradle/libs.versions.toml`,
обновления идут через Dependabot.

## Основа

| Что | Выбор | Почему |
|---|---|---|
| Язык | **Kotlin 2.4.x** | один язык для всех платформ, сервера и сборки; поддержка Xcode 26 |
| Мультиплатформа | **Kotlin Multiplatform** | общий код для Android, iOS, JVM desktop ([ADR 0001](../architecture/adr/0001-kotlin-multiplatform-compose.md)) |
| UI | **Compose Multiplatform 1.12+** | один UI-код; стабилен на iOS с 1.8, общий `@Preview` и Hot Reload с 1.10, нативный ввод текста на iOS с 1.11 |
| Сборка | Gradle 9 + Kotlin DSL, version catalog, convention-плагины в `build-logic` | стандарт для KMP |
| Среда разработки | Android Studio или IntelliJ IDEA + плагин Kotlin Multiplatform, Xcode 26 | запуск и отладка iOS из IDE на Mac |

## Библиотеки

| Задача | Выбор | Альтернатива |
|---|---|---|
| Асинхронность | kotlinx.coroutines + Flow | — |
| Сериализация | kotlinx.serialization (JSON) | — |
| Сеть (клиент) | **Ktor Client 3** (OkHttp на Android и desktop, Darwin на iOS) | — |
| Сеть (сервер приёма на ПК, relay) | **Ktor Server 3** | — |
| Локальная БД | **Room 3** (KSP, `BundledSQLiteDriver`) + Paging 3 ([ADR 0010](../architecture/adr/0010-room3.md)) | SQLDelight 2 |
| Настройки | DataStore (KMP) | multiplatform-settings |
| DI | **Koin 4** | Metro, kotlin-inject |
| Навигация | Navigation 3 (поддерживается в Compose Multiplatform с 1.10) | Decompose |
| ViewModel / жизненный цикл | androidx.lifecycle (KMP) | — |
| Загрузка изображений | **Coil 3** со своими загрузчиками для каждого источника | — |
| Видео | Media3 ExoPlayer (Android), AVPlayer (iOS), VLCJ (desktop) — ⚠️ спайк | — |
| Криптография | cryptography-kotlin (JDK, Apple, OpenSSL-провайдеры) | libsodium-kmp |
| TLS-сертификат ПК | BouncyCastle (`bcpkix`) на JVM: в JDK нет API для создания сертификатов (спайк S-03) | — |
| Работа с файлами | kotlinx-io | Okio |
| Логи | Kermit | — |
| QR: генерация | qrcode-kotlin | ZXing |
| QR: сканирование | CameraX + ZXing (Android), AVFoundation / VisionKit (iOS) | ML Kit — не берём: проприетарный, мешает F-Droid |
| mDNS | NsdManager (Android), Network.framework (iOS), JmDNS (desktop) | — |
| OAuth | системный браузер + PKCE: Custom Tabs (Android), `ASWebAuthenticationSession` (iOS), loopback-редирект (desktop) | AppAuth |
| Фоновая работа | WorkManager (Android), BGTaskScheduler + background URLSession (iOS) | — |
| Безопасное хранение | Keychain, Android Keystore, java-keyring (desktop) | — |
| Perceptual hash | своя реализация dHash/pHash на общем Kotlin | — |

## Качество

| Что | Инструмент |
|---|---|
| Тесты | kotlin.test, Turbine (Flow), kotlinx-coroutines-test |
| UI-тесты | Compose UI testing (multiplatform) |
| Скриншот-тесты компонентов | Roborazzi (JVM / Android) |
| E2E на мобильных | Maestro |
| Стиль и анализ | ktlint (через Spotless), detekt |
| Покрытие | Kover |
| Безопасность | CodeQL, Dependabot, Gradle dependency verification |

## Desktop

| Что | Выбор |
|---|---|
| UI | Compose for Desktop (JVM 25, встроенная JRE) |
| Упаковка | Compose Gradle plugin (jpackage): `.dmg` (macOS), `.msi` (Windows), `.deb` (Linux) |
| Подпись macOS | Developer ID + нотаризация (`notarytool`) |
| Microsoft Store | MSIX, подпись выполняет Microsoft |
| Трей / строка меню | Compose `Tray` на Windows и Linux; на macOS — `NSStatusItem` с панелью на SwiftUI |
| Автозапуск | LaunchAgent (macOS), реестр / ярлык в автозагрузке (Windows) |
| Горячая перезагрузка UI | Compose Hot Reload |

## Сервер (v1.1, relay)

Kotlin + Ktor Server, Docker, без базы данных. См. [backend.md](../architecture/backend.md).

## Сайт

Статический лендинг (Astro или аналог) на **GitHub Pages**. Выбор — на этапе дизайна.
Веб-приложение в будущем — Compose Multiplatform Web (Wasm), с общим кодом UI.

## Минимальные версии платформ

| Платформа | Минимум | Сборка |
|---|---|---|
| Android | API 26 (8.0) | target — последний API |
| iOS | 16 | Xcode 26+, iOS 26 SDK (обязательно для App Store с апреля 2026) |
| macOS | 12 | — |
| Windows | 10 64-bit | — |

## Что нужно для разработки

| Что | Зачем | Когда |
|---|---|---|
| **ПК на Windows + Android Studio** (плагин Kotlin Multiplatform) | общий код, Android, desktop-версия для Windows и Linux | сейчас |
| JDK 25 (LTS), через toolchain Gradle | Gradle, desktop; байткод Android — уровня 17 | сейчас |
| Android-телефон (или эмулятор) | отладка, реальная производительность, камера для QR | сейчас |
| Mac на Apple Silicon, 16+ ГБ ОЗУ | сборка iOS (Kotlin/Native) и macOS, Xcode, симулятор | есть, [трек Apple](../development-plan.md#трек-apple--параллельно-и-бесплатно) |
| iPhone | PhotoKit с iCloud, камера, производительность; установка с бесплатным Apple ID | есть |
| Apple Developer Program | TestFlight, App Store, нотаризация `.dmg` | этап «iOS и macOS», $99 в год |

На Windows Android Studio собирает и запускает Android- и desktop-версии; iOS-модули в проекте есть,
но собираются только на macOS (локально или на macOS-раннере GitHub Actions).

## iOS и macOS: что делаем на SwiftUI

Правило: интерфейс на Compose, а то, что Compose на iOS и Mac не умеет или делает хуже системы, — на SwiftUI.

- **iPhone и iPad:** навигация на SwiftUI (`TabView`, `NavigationStack`, `.toolbar`, `.sheet`) — так панели
  получают настоящий Liquid Glass. Экраны внутри — Compose через `ComposeUIViewController`.
- **macOS:** основное окно — Compose Desktop. Стекло окна, панель в строке меню и окно настроек — SwiftUI
  из Swift-библиотеки `apps/macos-native`, которую desktop-приложение вызывает через Foreign Function & Memory API.

Подробности и спайки — [ADR 0009](../architecture/adr/0009-liquid-glass-native-navigation.md).

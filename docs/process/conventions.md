# Конвенции разработки

Правила, по которым живёт репозиторий. Они короткие специально — чтобы их реально соблюдать.
Если правило мешает, его меняют через PR в этот файл, а не нарушают молча.

## Коротко

| Что | Правило | Пример |
|---|---|---|
| Ветка | `<тип>/<issue>-<описание>` | `feat/12-qr-pairing` |
| Коммит | [Conventional Commits](https://www.conventionalcommits.org/ru/v1.0.0/), на английском | `feat(devices): add QR pairing screen` |
| PR | заголовок = коммит, слияние через squash | `fix(ios): keep Live Photo pairs together` |
| Версия | [SemVer](https://semver.org/lang/ru/), тег `vX.Y.Z` | `v1.0.0`, `v1.1.0-beta.2` |
| Язык | документация — русский; код, комментарии, коммиты — английский | — |

---

## 1. Ветки

### Модель: trunk-based

- `main` — единственная долгоживущая ветка. Она **всегда собирается** и проходит CI.
- В `main` нельзя пушить напрямую — только через Pull Request (включается защитой ветки).
- Рабочие ветки — **короткие**: от нескольких часов до нескольких дней. Большую задачу режем на части
  и вливаем по кусочку (незаконченное прячем за feature-флагом).
- После слияния ветка удаляется (в настройках репозитория включено *Automatically delete head branches*).

### Имя ветки

```
<тип>/<номер-issue>-<краткое-описание>
```

- только строчная латиница, цифры и дефисы;
- номер issue — если задача заведена (желательно);
- описание — 2–5 слов по-английски, до ~50 символов всего.

| Тип | Когда | Пример |
|---|---|---|
| `feat/` | новая функциональность | `feat/12-qr-pairing` |
| `fix/` | исправление бага | `fix/34-ios-thumbnail-crash` |
| `docs/` | только документация | `docs/update-roadmap` |
| `design/` | дизайн-система, иконки, макеты в репо | `design/color-tokens` |
| `refactor/` | переработка без изменения поведения | `refactor/sources-interface` |
| `perf/` | производительность | `perf/grid-thumbnail-cache` |
| `test/` | только тесты | `test/dedupe-phash-cases` |
| `build/` | Gradle, зависимости, упаковка | `build/desktop-msi` |
| `ci/` | GitHub Actions | `ci/ios-testflight` |
| `chore/` | прочее обслуживание | `chore/update-gitignore` |
| `spike/` | прототип-исследование, **не вливается** в `main` как есть | `spike/ios-background-upload` |
| `release/` | стабилизация релиза (только если нужна) | `release/1.0.0` |
| `hotfix/` | срочное исправление выпущенной версии | `hotfix/1.0.1-crash-on-start` |

Автоматические ветки создают инструменты: `claude/*` (ИИ-агент), `dependabot/*`, `release-please--*`.
Их не переименовываем.

---

## 2. Коммиты

Используем [Conventional Commits 1.0.0](https://www.conventionalcommits.org/ru/v1.0.0/). Из этого формата
автоматически собираются CHANGELOG и номер следующей версии.

### Формат

```
<type>(<scope>)!: <subject>

<body>

<footer>
```

- **type** — тип изменения (таблица ниже), обязателен;
- **scope** — область (модуль или платформа), желателен;
- **!** — ставится, если изменение ломает совместимость;
- **subject** — что сделано, обязателен;
- **body** — зачем и как, по желанию;
- **footer** — ссылки на issue, `BREAKING CHANGE:`, соавторы.

### Типы

| type | Смысл | Влияние на версию |
|---|---|---|
| `feat` | новая возможность для пользователя | minor (`1.2.0 → 1.3.0`) |
| `fix` | исправление бага | patch (`1.2.0 → 1.2.1`) |
| `perf` | ускорение без изменения поведения | patch |
| `refactor` | переработка кода без изменения поведения | — |
| `docs` | документация | — |
| `design` | дизайн-система, иконки, стили | — |
| `test` | тесты | — |
| `build` | сборка, зависимости, упаковка | — |
| `ci` | CI/CD | — |
| `chore` | всё остальное служебное | — |
| `revert` | откат предыдущего коммита | зависит от откатываемого |

`!` после scope или футер `BREAKING CHANGE:` → **major** (`1.x → 2.0.0`).

### Области (scope)

Совпадают с модулями и платформами — см. [структуру проекта](../tech/project-structure.md).

| Группа | scope |
|---|---|
| Продуктовые модули | `library`, `sources`, `dedupe`, `devices`, `transfer`, `archive`, `cleanup`, `settings`, `onboarding` |
| Коннекторы | `yandex`, `dropbox`, `onedrive`, `gphotos`, `takeout`, `webdav`, `folder`, `gallery` |
| Основа | `core`, `db`, `ds` (дизайн-система), `platform` |
| Платформы | `android`, `ios`, `desktop`, `macos`, `windows`, `server`, `website` |
| Служебные | `deps`, `release`, `docs` |

Если изменение затрагивает много областей — scope можно не указывать.

### Правила для subject

- **по-английски, в повелительном наклонении**: `add`, `fix`, `remove`, а не `added`, `fixes`;
- с маленькой буквы, без точки в конце;
- вся первая строка — **не длиннее 72 символов**;
- отвечает на вопрос «что сделает этот коммит, если его применить?».

Тело (body) можно писать по-русски, если так понятнее. Строки переносим примерно на 72 символах.

### Футеры

| Футер | Когда |
|---|---|
| `Closes #12` | коммит/PR закрывает issue |
| `Refs #34` | связан с issue, но не закрывает |
| `BREAKING CHANGE: <что сломалось и как мигрировать>` | несовместимое изменение |
| `Co-authored-by: Имя <email>` | соавтор |

### Примеры

```
feat(devices): add QR pairing between phone and desktop

The desktop shows a one-time QR code; the phone scans it, pins the
desktop certificate and exchanges public keys.

Closes #12
```

```
fix(ios): keep Live Photo image and video together when sending
```

```
perf(library): cache decoded thumbnails on disk
```

```
feat(transfer)!: switch upload protocol to resumable chunks

BREAKING CHANGE: desktop apps older than 0.4.0 can't receive files
from this version. Update the desktop app first.
```

```
docs: describe branch and commit conventions
```

### Хорошие привычки

- Один коммит — одно логическое изменение. Форматирование отдельно от логики.
- В `main` не попадают `wip`, `fix typo`, `asdf`: при слиянии PR сжимается в один аккуратный коммит.
- Никогда не коммитим секреты: ключи, пароли, `local.properties`, `*.jks`, `*.p8`, `.env`
  (они уже в `.gitignore`). Если секрет утёк — сразу отзываем его, а не только удаляем из истории.

### Шаблон сообщения

В корне лежит [`.gitmessage`](../../.gitmessage) — подсказка формата прямо в редакторе коммита:

```bash
git config commit.template .gitmessage
```

---

## 3. Pull Request

- **Заголовок PR = будущий коммит** в формате Conventional Commits (его проверяет
  workflow [`pr-title`](../../.github/workflows/pr-title.yml)). При squash-слиянии заголовок становится
  сообщением коммита в `main`.
- Описание — по [шаблону](../../.github/pull_request_template.md): что, зачем, как проверить, скриншоты для UI.
- Размер — желательно до ~400 изменённых строк. Большое — режем на несколько PR.
- Незаконченная работа — как **Draft PR**.
- Слияние — только **Squash and merge**, после зелёного CI.
- Поведение изменилось → в том же PR обновляем документацию в `docs/`.
- Архитектурное решение → в том же PR добавляем [ADR](../architecture/adr/README.md).

### Definition of Done

- [ ] Код собирается на всех затронутых платформах, CI зелёный.
- [ ] Новая логика покрыта тестами (общий код — обязательно).
- [ ] UI проверен на светлой и тёмной теме, на RU и EN, на телефоне и десктопе (если применимо).
- [ ] Строки интерфейса вынесены в ресурсы, нет захардкоженного текста.
- [ ] Доступность: у интерактивных элементов есть описания для скринридеров.
- [ ] Документация и CHANGELOG-заметка (через заголовок PR) актуальны.

---

## 4. Версии и релизы

- Нумерация по [SemVer](https://semver.org/lang/ru/): `MAJOR.MINOR.PATCH`.
- **Одна версия для всех приложений** (Android, iOS, desktop) — проще поддерживать и объяснять пользователям.
- Теги: `v1.0.0`. Предрелизы: `v1.0.0-alpha.1`, `v1.0.0-beta.3`, `v1.0.0-rc.1`.
- **Номер сборки** (Android `versionCode`, iOS `CFBundleVersion`) — монотонно растущее целое из CI,
  одинаковое для всех платформ одной сборки.
- До `1.0.0` версии имеют вид `0.x.y`: minor может ломать совместимость.
- `CHANGELOG.md` генерируется из коммитов (release-please, см. [CI/CD](../tech/ci-cd.md)), руками не правим.
- `release/x.y.z` создаём, только если нужна стабилизация параллельно с разработкой.
  Хотфикс: ветка `hotfix/x.y.z-...` от тега, PR в `main`, новый тег.

---

## 5. Issues, лейблы, вехи

- Всё, что больше пары часов работы, — заводим issue (шаблоны: баг / предложение).
- Крупные этапы — эпики-issue с чек-листами (по фазам [роадмапа](../roadmap.md)).
- Вехи (Milestones) = версии с названием этапа: `v0.1 — Ядро и лента`, …, `v1.0 — Первый релиз`
  (список — в [настройке репозитория](repository-setup.md#9-вехи)).

Полный список с цветами и описаниями — в [`.github/labels.yml`](../../.github/labels.yml)
(лейблы создаются автоматически, см. [настройку репозитория](repository-setup.md#8-лейблы)).

| Группа | Лейблы |
|---|---|
| Тип | `bug`, `enhancement`, `documentation`, `design`, `spike`, `tech-debt`, `epic` |
| Платформа | `platform:android`, `platform:ios`, `platform:desktop`, `platform:web` |
| Область | `area:sources`, `area:dedupe`, `area:transfer`, `area:ui` |
| Приоритет | `P0` (блокер), `P1`, `P2`, `P3` |
| Прочее | `good first issue`, `help wanted`, `blocked`, `question` |

---

## 6. Код

### Kotlin

- [Официальный стиль Kotlin](https://kotlinlang.org/docs/coding-conventions.html), автоформат — ktlint, анализ — detekt (оба в CI).
- Отступ 4 пробела, длина строки до 120, trailing commas разрешены (см. [`.editorconfig`](../../.editorconfig)).
- Пакеты: `app.homeroll.<модуль>[.<подмодуль>]`, например `app.homeroll.sources.yandex`.
- Именование:
  - экраны: `LibraryScreen`, состояние `LibraryUiState`, логика `LibraryViewModel`;
  - компоненты дизайн-системы — существительные с префиксом `Hr`: `HrButton`, `HrMediaThumbnail`;
  - сценарии (use cases) — глагол: `FindDuplicates`, `SendToDevice`;
  - `expect`/`actual` и платформенные реализации — суффикс платформы в имени файла: `PhotoLibrary.ios.kt`.
- Ресурсы строк — `snake_case` с префиксом экрана: `library_empty_title`, `cleanup_confirm_button`.
  Русские множественные формы — только через `plurals`.
- Публичный API модулей — с KDoc. Комментарии в коде — по-английски и о том, **почему**, а не **что**.

### Swift (оболочка iOS)

- [Swift API Design Guidelines](https://www.swift.org/documentation/api-design-guidelines/), автоформат — SwiftFormat.
- Swift-кода минимум: точка входа и то, что невозможно сделать из Kotlin.

### Тесты

- Имя класса: `<Класс>Test`. Имя теста — предложение в обратных кавычках:
  ``fun `groups identical photos from different sources`()``.
- Общий код тестируем в `commonTest`; платформенный — в `androidUnitTest` / `iosTest` / `desktopTest`.

---

## 7. Документация

- Вся проектная документация — в [`docs/`](../README.md), на русском.
- `README.md` — на английском, [`README.ru.md`](../../README.ru.md) — его русская версия; меняем обе.
- Решения, которые сложно отменить (выбор библиотеки, протокола, формата данных), фиксируем в
  [ADR](../architecture/adr/README.md).
- Диаграммы — в Mermaid прямо в Markdown (GitHub их рендерит).

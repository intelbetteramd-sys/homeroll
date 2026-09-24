# Настройка репозитория на GitHub

Разовая настройка, которую делает владелец репозитория в веб-интерфейсе GitHub.
Выполнять **по порядку**: правила защиты веток бесплатно работают только в публичных репозиториях,
поэтому сначала открываем репозиторий.

Отмечайте выполненное в эпике Фазы 0.

## 1. Название и видимость

**Settings → General**

- [ ] **Repository name:** `homeroll` → *Rename*. Старые ссылки и `git remote` GitHub будет перенаправлять автоматически.
- [ ] **Danger Zone → Change repository visibility → Public.**
  Перед этим убедитесь, что в истории нет секретов (сейчас там только документация).

## 2. Описание (About)

Шестерёнка ⚙️ справа от списка файлов на главной странице репозитория.

- [ ] **Description:**
  ```
  Every photo from every cloud in one feed — no duplicates, no subscription, originals at home.
  ```
- [ ] **Website:** пока пусто; потом — адрес сайта (`https://homeroll.app` или GitHub Pages).
- [ ] **Topics:**
  ```
  kotlin-multiplatform compose-multiplatform photos photo-backup deduplication local-first privacy android ios desktop
  ```
- [ ] Галочки *Releases* оставить, *Packages* и *Deployments* можно убрать.

## 3. Возможности

**Settings → General → Features**

- [ ] **Issues** — включено.
- [ ] **Discussions** — включить: вопросы и идеи пользователей, чтобы не засорять issues.
- [ ] **Projects** — по желанию (доска с эпиками).
- [ ] **Wikis** — выключить: вся документация живёт в `docs/`.
- [ ] **Sponsorships** — выключить.

## 4. Pull Requests

**Settings → General → Pull Requests**

- [ ] ☐ Allow merge commits — **выключить**.
- [ ] ☑ Allow squash merging — **включить**, *Default commit message*: **Pull request title**
  (история `main` остаётся чистой; ломающие изменения помечаем `!` в заголовке).
- [ ] ☐ Allow rebase merging — **выключить**.
- [ ] ☑ Always suggest updating pull request branches.
- [ ] ☑ Allow auto-merge.
- [ ] ☑ Automatically delete head branches.

Итог: в `main` попадает ровно один коммит на PR, его заголовок — заголовок PR в формате
[Conventional Commits](conventions.md#2-коммиты).

## 5. Защита ветки `main`

**Settings → Rules → Rulesets → New ruleset → New branch ruleset**

- [ ] **Ruleset name:** `main`
- [ ] **Enforcement status:** Active
- [ ] **Bypass list:** пусто (или Repository admin — только для аварий)
- [ ] **Target branches → Add target → Include default branch**
- [ ] Правила:
  - ☑ **Restrict deletions**
  - ☑ **Require linear history**
  - ☑ **Require a pull request before merging**
    - Required approvals: **0** — вы единственный разработчик и не можете одобрить свой же PR.
      Когда появятся другие участники — поставить 1.
    - ☑ Require conversation resolution before merging
    - Allowed merge methods: **Squash**
  - ☑ **Require status checks to pass** → *Add checks* → `Conventional Commits`
    (проверка появится в списке после первого PR; когда появится `ci.yml`, добавить и его задачи).
  - ☑ **Block force pushes**
- [ ] **Не включать** (оставить выключенными):
  - ☐ **Restrict updates** — ⚠️ заблокирует вообще любые изменения `main`, включая слияние PR;
  - ☐ Restrict creations — `main` уже существует, правило ничего не даёт;
  - ☐ Require deployments to succeed — окружений пока нет;
  - ☐ Require signed commits — не нужно: в `main` попадают только squash-коммиты, которые GitHub подписывает сам;
  - ☐ Require code scanning results — включим после настройки CodeQL (Фаза 1);
  - ☐ Require code quality results, Restrict code coverage — когда появится код и CI;
  - ☐ Automatically request Copilot code review — не нужно.
- [ ] Внутри **Require a pull request before merging** выключить всё, что требует второго человека:
  Dismiss stale approvals, Require review from specific teams, Require review from Code Owners,
  Require approval of the most recent reviewable push, дополнительное одобрение для PR от Copilot.
  Иначе вы не сможете влить собственный PR.
- [ ] *Create*.

## 6. Безопасность

**Settings → Advanced Security**

| Пункт | Выбор | Зачем |
|---|---|---|
| Private vulnerability reporting | ✅ Enable | приватные отчёты об уязвимостях, на это ссылается [SECURITY.md](../../SECURITY.md) |
| Dependency graph | ✅ Enable | список зависимостей проекта |
| Automatic dependency submission | ✅ Enable | Gradle-зависимости попадут в граф автоматически, когда появится код |
| Dependabot alerts | ✅ Enable | уведомления об уязвимых зависимостях |
| Dependabot rules | оставить как есть | встроенное правило скрывает малозначимые оповещения |
| Dependabot malware alerts | ✅ Enable | оповещение, если в зависимость подсунули вредонос |
| Dependabot security updates | ✅ Enable | Dependabot сам откроет PR с исправлением уязвимой зависимости |
| Grouped security updates | ✅ Enable | одно PR вместо десятка |
| Dependabot version updates | не нажимать | включается файлом `.github/dependabot.yml`, добавляем его через PR |
| Code scanning → CodeQL analysis | ✅ *Set up → Default* | сейчас проверяет GitHub Actions workflow; когда появится Kotlin — проверить, что язык подхватился |
| Other tools | — | не нужно |
| AI Scan for pull requests (Preview) | ❌ Off | Kotlin покрывается CodeQL, превью даст лишний шум |
| Copilot Autofix | ✅ On | только предлагает исправления к находкам CodeQL |
| Check runs failure threshold | по умолчанию | — |
| Secret Protection: Secret scanning | ✅ Enable | найдёт случайно закоммиченные токены и ключи |
| Secret Protection: Push protection | ✅ Enable | не даст запушить токен или ключ |

Для публичных репозиториев всё это бесплатно.

## 7. GitHub Actions

**Settings → Actions → General**

- [ ] **Actions permissions:** Allow all actions and reusable workflows.
- [ ] **Workflow permissions:** *Read repository contents and packages permissions*.
- [ ] ☑ **Allow GitHub Actions to create and approve pull requests** — понадобится release-please
  для PR с новой версией.

## 8. Лейблы

Вручную ничего создавать не нужно. Лейблы описаны в файле [`.github/labels.yml`](../../.github/labels.yml),
и workflow [`Labels`](../../.github/workflows/labels.yml) создаёт и обновляет их при каждом изменении этого файла
в `main`. Лейблы, которых нет в файле, не удаляются.

- [ ] Проверить, что лейблы появились: **Issues → Labels**.
      Если нет — **Actions → Labels → Run workflow**.

Чтобы добавить или изменить лейбл — правим `.github/labels.yml` через PR.

## 9. Вехи

Тоже автоматически. После синхронизации лейблов workflow
[`Roadmap setup`](../../.github/workflows/roadmap-setup.yml) создаёт вехи и вешает на эпики #1–#8 лейблы и вехи
по таблице ниже. Повторный запуск ничего не ломает: существующие вехи остаются, лейблы только добавляются,
а эпик, у которого веха уже есть, не переносится.

- [ ] Проверить: **Issues → Milestones** — 5 вех, в каждой свои эпики.
      Если нет — **Actions → Roadmap setup → Run workflow**.

Формат названия вехи — версия и короткое название этапа.

| Title | Description | Эпики |
|---|---|---|
| `v0.1 — Ядро и лента` | ядро, база данных, галерея, единая лента | #1, #2, #3, #4 |
| `v0.2 — Устройства и передача` | связка телефона и ПК, передача, автоархив | #5 |
| `v0.3 — Облака` | Яндекс Диск, Dropbox, OneDrive, WebDAV, Google | #6 |
| `v0.4 — Дубли и уборка` | дубли и безопасное освобождение места | #7 |
| `v1.0 — Первый релиз` | бета, полировка и публикация в сторах | #8 |

## 10. Позже

- **Social preview** (Settings → General → Social preview): картинка 1280×640 для ссылок в мессенджерах —
  после дизайна логотипа.
- **Pages** (Settings → Pages): сайт из `website/` через GitHub Actions — в Фазе 7.
- **Environments** и секреты для релизов — по мере регистрации в сторах ([CI/CD](../tech/ci-cd.md#секреты-ci)).

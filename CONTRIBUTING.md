# Contributing to Pixroost

**English summary.** Thanks for your interest! Pixroost is in the planning stage. Project docs are in Russian,
but issues and pull requests in English are welcome. Please follow the
[conventions](docs/process/conventions.md): branches `type/issue-short-name`, commits and PR titles in
[Conventional Commits](https://www.conventionalcommits.org/) format (in English), squash merges only.
Report security issues privately — see [SECURITY.md](SECURITY.md).

---

# Как участвовать в Pixroost

Спасибо, что заглянули! Проект на этапе планирования: код появится в [Фазе 1](docs/roadmap.md).
Уже сейчас можно обсуждать идеи, требования и архитектуру.

## С чего начать

1. Прочитайте [видение](docs/product/vision.md) и [роадмап](docs/roadmap.md).
2. Посмотрите открытые issues, особенно с лейблами `good first issue` и `help wanted`.
3. Перед большой работой напишите в issue, что берёте её на себя, чтобы не делать одно и то же дважды.

## Как предложить изменение

1. **Issue.** Баг или идея → issue по шаблону. Для мелких правок (опечатка в документации) можно сразу PR.
2. **Ветка** от `main` по [правилам именования](docs/process/conventions.md#1-ветки):
   `feat/12-qr-pairing`, `fix/34-crash-on-start`, `docs/update-roadmap`.
3. **Коммиты** — [Conventional Commits](docs/process/conventions.md#2-коммиты) на английском:
   `feat(devices): add QR pairing screen`. Подсказка формата: `git config commit.template .gitmessage`.
4. **Pull Request** в `main`: заголовок в том же формате (его проверяет CI), описание по шаблону,
   скриншоты для изменений интерфейса.
5. **Слияние** — squash, после зелёного CI и ревью.

## Что важно в этом проекте

- **Приватность.** Никаких SDK аналитики и рекламы, никакой отправки данных на сторонние серверы.
- **Безопасность удаления.** Всё, что касается удаления файлов, проходит через `CleanupSafetyPolicy` и
  обязательно покрыто тестами.
- **Документация.** Если меняется поведение — в том же PR обновляем `docs/`. Архитектурные решения — в
  [ADR](docs/architecture/adr/README.md).

## Лицензия

Отправляя изменения, вы соглашаетесь, что они распространяются под [Apache License 2.0](LICENSE)
(раздел 5 лицензии).

## Поведение

Мы следуем [кодексу поведения](CODE_OF_CONDUCT.md).

# Среда разработки: Windows и Mac

Что поставить на компьютер, чтобы собирать и запускать Pixroost для Android и Windows.
Для iPhone и macOS нужен Mac — [раздел 7](#7-mac-iphone-и-macos).

Команды для Windows написаны для **PowerShell** — это терминал Windows и Android Studio по умолчанию.
Gradle там запускается как `.\gradlew`: PowerShell не ищет программы в текущей папке, поэтому без `.\`
команда не найдётся. В старой командной строке (cmd) `.\gradlew` тоже работает.

## Что понадобится

| Что | Зачем | Где взять |
|---|---|---|
| Git для Windows | работа с репозиторием | [git-scm.com](https://git-scm.com/download/win) |
| JDK 25 (Eclipse Temurin) | Gradle из командной строки и desktop-приложение | [инструкция](jdk.md) |
| Android Studio, последняя стабильная | IDE, Android SDK, эмулятор | [developer.android.com/studio](https://developer.android.com/studio) |
| Плагин **Kotlin Multiplatform** | запуск desktop-приложения и общего кода из IDE | Android Studio → Settings → Plugins → Marketplace |
| Эмулятор Android | каждодневный запуск и отладка, кабель не нужен | ставится вместе с Android Studio |
| Android-телефон | проверки на настоящем устройстве: APK через Telegram | уже есть |

Всё бесплатно.

## 1. Git

1. Установить Git для Windows с настройками по умолчанию.
2. В терминале:

   ```bash
   git config --global user.name "Ваше имя"
   git config --global user.email "почта-из-GitHub"
   git config --global core.autocrlf false   # концы строк задаёт .gitattributes
   git config --global core.longpaths true   # длинные пути Gradle и Android
   ```

3. Склонировать репозиторий в короткий путь **без кириллицы и пробелов**, например `C:\dev\pixroost`.
   Пути с кириллицей ломают часть инструментов Android.

   ```bash
   git clone https://github.com/intelbetteramd-sys/pixroost.git C:\dev\pixroost
   cd C:\dev\pixroost
   git config commit.template .gitmessage     # подсказка формата коммита
   ```

## 2. JDK 25

Пошагово для Windows и Mac, с проверкой и разбором «почему сборка идёт на Java 21» —
в отдельной инструкции: **[JDK 25: как поставить](jdk.md)**.

Коротко для Windows: установщик Temurin 25 (`.msi`) с включённым пунктом *Set JAVA_HOME variable*,
затем в Android Studio *Gradle JDK* = 25.

**Почему 25, а не новее.** JDK 25 — LTS: обновления безопасности выходят годами. Desktop-версия везёт JDK
внутри установщика, поэтому версия с долгой поддержкой важна и для пользователей. 26 — промежуточный
выпуск: обновления для него закончились, когда 15 сентября 2026 вышла 27. А 27 Gradle 9.7 пока
не умеет запускать.

## 3. Android Studio

1. Установить Android Studio. В мастере первого запуска выбрать *Standard*: он поставит Android SDK,
   Platform-Tools и эмулятор.
2. Settings → Plugins → Marketplace → **Kotlin Multiplatform** → Install → перезапустить IDE.
3. Settings → Build, Execution, Deployment → Build Tools → Gradle → *Gradle JDK* = JDK 25
   ([подробнее](jdk.md#3-android-studio)).
4. Settings → Languages & Frameworks → Android SDK: в *SDK Platforms* отмечена последняя стабильная версия
   Android, в *SDK Tools* — Android SDK Build-Tools и Platform-Tools.
5. Задать `ANDROID_HOME`, чтобы `.\gradlew` в терминале находил Android SDK в любой папке проекта.
   Android Studio пишет путь в `local.properties` сама, но только после синхронизации проекта. Одной командой
   в PowerShell, затем открыть новое окно терминала:

   ```powershell
   [Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
   ```

   Путь к SDK показан в Settings → Languages & Frameworks → Android SDK → *Android SDK Location*.

## 4. Где запускать Android-версию

Кабель и отладка по USB **не обязательны**. Основной способ — эмулятор, а на настоящий телефон
приложение ставится APK-файлом, например через Telegram.

### Эмулятор — для каждодневной работы

1. Включить виртуализацию: Панель управления → Программы → Включение или отключение компонентов Windows →
   **Платформа низкоуровневой оболочки Windows** (Windows Hypervisor Platform) → перезагрузить ПК.
   Если эмулятор всё равно пишет про ускорение — включить виртуализацию в BIOS (Intel VT-x или AMD SVM).
2. Android Studio → Device Manager → **+** → Create Virtual Device → телефон Pixel → образ последней версии
   Android → Finish.
3. Запустить эмулятор кнопкой ▶. Фото для галереи — перетащить файлы мышью в окно эмулятора.

В эмуляторе работают отладчик, логи (Logcat) и запуск одной кнопкой. Чего в нём нет: настоящей камеры
и соседства с ПК в одной Wi-Fi-сети. Сканирование QR, поиск ПК и передачу фото проверяем на телефоне.

### Телефон — APK через Telegram

1. Собрать APK: в Android Studio *Build → Build APK(s)* или `.\gradlew :apps:android:assembleDebug`
   (в шаблоне из раздела 6 — `.\gradlew :androidApp:assembleDebug`). Файл `.apk` появится в папке
   `build\outputs\apk\debug` этого модуля.
2. Отправить его себе в Telegram («Избранное»), открыть на телефоне, разрешить Telegram установку приложений.
3. Новая сборка ставится поверх старой, данные сохраняются. Если телефон пишет «Приложение не установлено» —
   старая версия подписана другим ключом (например, собрана на другом ПК): удалить её и поставить заново.
4. Логи без кабеля в Android Studio не видны. Поэтому в приложении с первых версий будет
   «Диагностический журнал»: файл можно отправить себе в Telegram.

После шага 1.3 [плана](../development-plan.md) APK будет собирать GitHub на каждое изменение — его можно
скачать прямо на телефоне, без ПК.

### Если всё-таки нужна отладка по USB

Для отладки по USB SIM-карта не нужна. Но на **Xiaomi, Redmi и POCO** есть отдельный переключатель
«Установка через USB», и MIUI / HyperOS включает его только с SIM-картой и входом в Mi-аккаунт.
Достаточно один раз вставить любую SIM-карту на пару минут, включить переключатель и вынуть её.
Отладка по Wi-Fi (Android 11+, в Android Studio — *Pair Devices Using Wi-Fi*) на Xiaomi упирается
в тот же переключатель.

## 5. Чтобы сборка не тормозила

- Добавить в исключения Microsoft Defender (Безопасность Windows → Защита от вирусов и угроз → Исключения):
  папку проекта, `%USERPROFILE%\.gradle` и `%LOCALAPPDATA%\Android\Sdk`. Проверка антивирусом каждого файла
  замедляет Gradle в разы.
- Держать проект и `.gradle` на SSD.
- 16 ГБ памяти хватает; при 8 ГБ закрывать браузер во время сборки.

## 6. Проверка

**Проверка среды на шаблоне** (шаг 0.3 [плана](../development-plan.md); сам шаблон в Pixroost не добавляем):

1. На [kmp.jetbrains.com](https://kmp.jetbrains.com) заполнить форму по таблице ниже, скачать шаблон, открыть его
   в Android Studio. В репозиторий Pixroost его не добавляем.
2. Запустить конфигурацию Android на эмуляторе; собрать APK и поставить его на телефон через Telegram.
3. Запустить desktop-приложение: конфигурация *desktopApp* или `.\gradlew :desktopApp:run`.

Модули в шаблоне называются не так, как будут в Pixroost: `androidApp`, `desktopApp`, `shared`.
Точный список показывает `.\gradlew projects`. Команды с `:apps:…` — только для Pixroost после шага 1.1.

Предупреждение `Native task 'iosSimulatorArm64Test' is disabled` на Windows — нормально: iOS собирается
только на Mac. Убрать его можно строкой `kotlin.native.ignoreDisabledTargets=true` в `gradle.properties`.

| Поле мастера | Что указать |
|---|---|
| Project Name | `Pixroost` |
| Project ID | `app.pixroost` |
| Платформы | Android, iOS и Desktop. На Windows iOS-часть не собирается, но шаблону это не мешает |
| iOS: *Share UI* или *Do not share UI* | **Share UI** — интерфейс на Compose ([ADR 0001](../architecture/adr/0001-kotlin-multiplatform-compose.md)); нативную навигацию добавим отдельно ([ADR 0009](../architecture/adr/0009-liquid-glass-native-navigation.md)) |
| Server, Web | не нужны |

**После шага 1.1** (в папке Pixroost, не шаблона).

Первый раз открыть проект в Android Studio: File → Open → папка `pixroost` → дождаться, пока внизу закончится
синхронизация Gradle. Запустить её вручную: File → **Sync Project with Gradle Files** (в новом интерфейсе
меню File — под кнопкой ☰ слева вверху), сочетание `Ctrl+Shift+O` или круглые стрелки в панели **Gradle**
справа. После синхронизации в списке конфигураций запуска появится `android`.

Из терминала:

```powershell
.\gradlew :apps:android:installDebug   # поставить на запущенный эмулятор
.\gradlew :apps:android:assembleDebug  # собрать APK для телефона
.\gradlew :apps:desktop:run            # запустить на Windows
.\gradlew check                        # всё, что проверяет CI
```

## 7. Mac: iPhone и macOS

Всё бесплатно: Apple Developer Program ($99 в год) нужна только для публикации. Что делать по шагам —
[трек Apple](../development-plan.md#трек-apple--параллельно-и-бесплатно) в плане.

| Что | Зачем | Где взять |
|---|---|---|
| macOS, последняя стабильная | свежий Xcode требует свежую macOS | Системные настройки → Основные → Обновление ПО |
| Xcode, последний стабильный | сборка iOS, симулятор, установка на iPhone | App Store |
| JDK 25 (Eclipse Temurin) | Gradle | [инструкция для Mac](jdk.md#mac) |
| Android Studio или IntelliJ IDEA + плагин **Kotlin Multiplatform** | запуск iOS- и desktop-версий из IDE | [developer.android.com/studio](https://developer.android.com/studio) |
| Apple ID | подпись для своего iPhone | уже есть, бесплатно |

### Установка

1. Установить Xcode и один раз открыть: принять лицензию и поставить компоненты iOS, которые он предложит.
2. Проверить в терминале: `xcode-select -p` показывает путь внутри `Xcode.app`.
   Если нет — `sudo xcode-select --switch /Applications/Xcode.app`.
3. JDK 25 — по [инструкции для Mac](jdk.md#mac), IDE с плагином — как в разделе 3.
   `ANDROID_HOME` на Mac: `echo 'export ANDROID_HOME=$HOME/Library/Android/sdk' >> ~/.zshrc`.
4. Склонировать репозиторий, например в `~/dev/pixroost`.
5. Xcode → Settings → Accounts → **+** → Apple ID. Появится команда «Ваше имя (Personal Team)».

### Запуск на своём iPhone

1. Подключить iPhone кабелем и на iPhone нажать «Доверять этому компьютеру».
2. Включить режим разработчика: Настройки → Конфиденциальность и безопасность → **Режим разработчика** →
   перезагрузить iPhone. Пункт появляется после первого подключения к Xcode.
3. В Xcode-проекте (в шаблоне — `iosApp`, после шага A.2 — `apps/ios`) открыть *Signing & Capabilities*:
   *Team* — Personal Team, *Bundle Identifier* — `app.pixroost.dev`. Суффикс `.dev` не даёт бесплатному
   аккаунту занять `app.pixroost`. Команду и bundle ID держим в локальном `.xcconfig`, в репозиторий не коммитим.
4. Выбрать iPhone в списке устройств и нажать ▶. При первом запуске iPhone попросит доверять разработчику:
   Настройки → Основные → VPN и управление устройством → свой Apple ID → Доверять.
5. Дальше можно без кабеля: Xcode → Window → Devices and Simulators → *Connect via network*.

Ограничения бесплатного Apple ID:

- приложение работает **7 дней**, потом не открывается. Лечится повторной установкой из Xcode, данные сохраняются;
- на устройстве — не больше 3 своих приложений одновременно;
- нет TestFlight, App Store и push-уведомлений. Для разработки Pixroost они не нужны.

### macOS-версия

- После шага 1.1 `./gradlew :apps:desktop:run` запускает Pixroost на Mac. Для запуска на своём компьютере
  подпись не нужна.
- `.dmg` без подписи можно поставить себе: при первом открытии macOS его заблокирует, разрешить можно в
  Системные настройки → Конфиденциальность и безопасность → «Всё равно открыть». Чтобы ставить на чужие Mac,
  нужны подпись Developer ID и нотаризация, то есть $99.

## Если что-то не работает

| Симптом | Что сделать |
|---|---|
| `SDK location not found` | задать `ANDROID_HOME` (раздел 3, шаг 5) или один раз синхронизировать проект в Android Studio — она создаст `local.properties` |
| В Android Studio нет синхронизации Gradle и панели **Gradle** | проект открыт не как Gradle-проект: File → Open → выбрать `settings.gradle.kts` в папке `pixroost` → *Open as Project*; или нажать *Load Gradle Project* во всплывающем уведомлении |
| Эмулятор не запускается или просит ускорение | включить «Платформу низкоуровневой оболочки Windows» и виртуализацию в BIOS |
| Эмулятор тормозит | закрыть лишние программы; эмулятору нужно 2–4 ГБ памяти |
| APK: «Приложение не установлено» | удалить старую версию: её подписал другой отладочный ключ |
| Xiaomi не даёт включить «Установка через USB» | нужна SIM-карта и Mi-аккаунт, см. раздел 4 |
| `gradlew.bat is not recognized` | в PowerShell писать `.\gradlew` |
| `project 'apps' not found` | команды `:apps:…` — для Pixroost после шага 1.1; в шаблоне — `:desktopApp:run`, `:androidApp:installDebug` |
| Gradle берёт Java 21, хотя стоит 25 | [откуда она берётся и что сделать](jdk.md#если-всё-равно-21) |
| Gradle ругается на версию Java | *Gradle JDK* = 25 в настройках, `JAVA_HOME` указывает на JDK 25 (или 26). На Java 27 Gradle 9.7 не запускается |
| `Filename too long` | `git config --global core.longpaths true`, короткий путь к проекту |
| Сборка очень долгая | исключения в Defender (раздел 5) |
| iPhone: приложение перестало открываться через неделю | срок бесплатной подписи 7 дней: запустить из Xcode ещё раз |
| Xcode: *No Account for Team* или *Failed to register bundle identifier* | войти Apple ID в Settings → Accounts; если bundle ID занят — другой суффикс, например `app.pixroost.dev.ivan` |
| Xcode не видит iPhone | «Доверять этому компьютеру» на iPhone, включён режим разработчика, кабель с передачей данных |

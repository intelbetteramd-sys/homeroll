# Среда разработки на Windows

Что поставить на компьютер, чтобы собирать и запускать Pixroost для Android и Windows.
Mac и Xcode нужны только на [этапе «iOS и macOS»](../roadmap.md#этап-ios-и-macos).

## Что понадобится

| Что | Зачем | Где взять |
|---|---|---|
| Git для Windows | работа с репозиторием | [git-scm.com](https://git-scm.com/download/win) |
| JDK 21 (Eclipse Temurin) | Gradle из командной строки и desktop-приложение | [adoptium.net](https://adoptium.net/temurin/releases/?version=21) |
| Android Studio, последняя стабильная | IDE, Android SDK, эмулятор | [developer.android.com/studio](https://developer.android.com/studio) |
| Плагин **Kotlin Multiplatform** | запуск desktop-приложения и общего кода из IDE | Android Studio → Settings → Plugins → Marketplace |
| Android-телефон с кабелем | тесты на настоящем устройстве | уже есть |

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

## 2. JDK 21

1. Установить Temurin 21 (MSI). В установщике включить *Set JAVA_HOME* и *Add to PATH*.
2. Проверить в новом терминале: `java -version` показывает 21.

Android Studio приносит свою JDK, но отдельная JDK 21 нужна, чтобы `gradlew` работал из терминала так же, как в CI.

## 3. Android Studio

1. Установить Android Studio. В мастере первого запуска выбрать *Standard*: он поставит Android SDK,
   Platform-Tools и эмулятор.
2. Settings → Plugins → Marketplace → **Kotlin Multiplatform** → Install → перезапустить IDE.
3. Settings → Build, Execution, Deployment → Build Tools → Gradle → *Gradle JDK* = JDK 21.
4. Settings → Languages & Frameworks → Android SDK: в *SDK Platforms* отмечена последняя стабильная версия
   Android, в *SDK Tools* — Android SDK Build-Tools и Platform-Tools.

## 4. Телефон

1. На телефоне: Настройки → О телефоне → 7 раз нажать «Номер сборки» → появятся «Параметры разработчика».
2. В параметрах разработчика включить **Отладку по USB**.
3. Подключить телефон кабелем, разрешить отладку для этого компьютера.
4. Проверить: `adb devices` показывает телефон со статусом `device`
   (`adb` лежит в `%LOCALAPPDATA%\Android\Sdk\platform-tools`).

Эмулятор тоже подходит, но галерею, камеру для QR и передачу по Wi-Fi удобнее проверять на телефоне.

## 5. Чтобы сборка не тормозила

- Добавить в исключения Microsoft Defender (Безопасность Windows → Защита от вирусов и угроз → Исключения):
  папку проекта, `%USERPROFILE%\.gradle` и `%LOCALAPPDATA%\Android\Sdk`. Проверка антивирусом каждого файла
  замедляет Gradle в разы.
- Держать проект и `.gradle` на SSD.
- 16 ГБ памяти хватает; при 8 ГБ закрывать браузер во время сборки.

## 6. Проверка

**Пока в репозитории нет кода** (до шага 1.1 [плана](../development-plan.md)):

1. На [kmp.jetbrains.com](https://kmp.jetbrains.com) выбрать Android и Desktop, скачать шаблон, открыть его
   в Android Studio. В репозиторий Pixroost его не добавляем.
2. Запустить конфигурацию Android на телефоне.
3. Запустить desktop-приложение: конфигурация *desktop* или `gradlew.bat run` в модуле `composeApp`.

**После шага 1.1:**

```bash
gradlew.bat :apps:android:installDebug   # поставить на телефон
gradlew.bat :apps:desktop:run            # запустить на Windows
gradlew.bat check                        # всё, что проверяет CI
```

## Если что-то не работает

| Симптом | Что сделать |
|---|---|
| `adb devices` пустой | другой кабель (не только для зарядки), драйвер Google USB Driver в SDK Manager, заново разрешить отладку |
| Gradle ругается на версию Java | *Gradle JDK* = 21 в настройках, `JAVA_HOME` указывает на JDK 21 |
| `Filename too long` | `git config --global core.longpaths true`, короткий путь к проекту |
| Сборка очень долгая | исключения в Defender (раздел 5) |

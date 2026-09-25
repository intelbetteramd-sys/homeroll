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

## 4. Где запускать Android-версию

Кабель и отладка по USB **не обязательны**. Основной способ — эмулятор, а на настоящий телефон
приложение ставится APK-файлом, как вы привыкли.

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

1. Собрать APK: в Android Studio *Build → Build APK(s)* или `gradlew.bat :apps:android:assembleDebug`.
   Файл `.apk` появится в `apps\android\build\outputs\apk\debug`.
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

**Пока в репозитории нет кода** (до шага 1.1 [плана](../development-plan.md)):

1. На [kmp.jetbrains.com](https://kmp.jetbrains.com) выбрать Android и Desktop, скачать шаблон, открыть его
   в Android Studio. В репозиторий Pixroost его не добавляем.
2. Запустить конфигурацию Android на эмуляторе; собрать APK и поставить его на телефон через Telegram.
3. Запустить desktop-приложение: конфигурация *desktop* или `gradlew.bat run` в модуле `composeApp`.

**После шага 1.1:**

```bash
gradlew.bat :apps:android:installDebug   # поставить на запущенный эмулятор
gradlew.bat :apps:android:assembleDebug  # собрать APK для телефона
gradlew.bat :apps:desktop:run            # запустить на Windows
gradlew.bat check                        # всё, что проверяет CI
```

## Если что-то не работает

| Симптом | Что сделать |
|---|---|
| Эмулятор не запускается или просит ускорение | включить «Платформу низкоуровневой оболочки Windows» и виртуализацию в BIOS |
| Эмулятор тормозит | закрыть лишние программы; эмулятору нужно 2–4 ГБ памяти |
| APK: «Приложение не установлено» | удалить старую версию: её подписал другой отладочный ключ |
| Xiaomi не даёт включить «Установка через USB» | нужна SIM-карта и Mi-аккаунт, см. раздел 4 |
| Gradle ругается на версию Java | *Gradle JDK* = 21 в настройках, `JAVA_HOME` указывает на JDK 21 |
| `Filename too long` | `git config --global core.longpaths true`, короткий путь к проекту |
| Сборка очень долгая | исключения в Defender (раздел 5) |

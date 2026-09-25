# JDK 25: как поставить на Windows и Mac

Pixroost собирается на **JDK 25** — это версия с долгой поддержкой (LTS). Ставим сборку **Eclipse Temurin**:
она бесплатная и без ограничений лицензии.

Коротко:

- **Куда ставить** — никуда специально: установщик сам кладёт JDK в системную папку. Путь менять не нужно.
- **Что потом** — сказать Android Studio и терминалу, что нужна именно 25: настройка *Gradle JDK*
  и переменная `JAVA_HOME`.
- **Старые Java (21, 26) удалять не обязательно.** Важно только, чтобы Android Studio и `JAVA_HOME`
  смотрели на 25.

## Откуда берётся Java 21

В Android Studio встроена своя JDK — обычно 21. На ней работает сама IDE, и по умолчанию на ней же
запускается Gradle. Поэтому сборка идёт на 21, даже если вы поставили 25. Встроенную JDK трогать не нужно:
достаточно выбрать 25 в настройке *Gradle JDK* (шаг 3 ниже).

## Windows

### 1. Скачать

[adoptium.net → Temurin 25, Windows, x64, JDK, `.msi`](https://adoptium.net/temurin/releases/?version=25&os=windows&arch=x64&package=jdk).

### 2. Установить

1. Запустить `.msi`.
2. На экране *Custom Setup* нажать на пункт **Set JAVA_HOME variable** и выбрать
   *Will be installed on local hard drive*. По умолчанию этот пункт выключен.
3. Пункт **Add to PATH** уже включён — оставить.
4. Папку не менять. JDK ляжет в `C:\Program Files\Eclipse Adoptium\jdk-25.0.x-hotspot\`.

### 3. Android Studio

1. File → Settings → Build, Execution, Deployment → Build Tools → **Gradle**.
2. *Gradle JDK* → выбрать **Eclipse Temurin 25** (или *Add JDK from disk…* → папка из шага 2).
3. OK, затем *Sync Project with Gradle Files* (значок слона справа вверху).

### 4. Проверить в терминале

Открыть **новое** окно PowerShell (старое не видит новых переменных):

```powershell
java -version            # openjdk version "25..."
echo $env:JAVA_HOME      # C:\Program Files\Eclipse Adoptium\jdk-25...
```

В папке проекта:

```powershell
.\gradlew --stop         # остановить старые процессы Gradle на 21
.\gradlew --version      # строки Launcher JVM и Daemon JVM — 25
```

Если `java -version` и обе строки `.\gradlew --version` показывают 25, всё в порядке — даже когда
`JAVA_HOME` пустая: тогда `.\gradlew` берёт Java из `PATH`. Но `JAVA_HOME` лучше всё же задать — её ищут
и другие инструменты. Одной командой в PowerShell (путь берётся из той `java`, что уже находится):

```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", (Split-Path (Split-Path (Get-Command java).Source)), "User")
```

Перед этим стоит проверить, что `(Get-Command java).Source` показывает путь внутри `Eclipse Adoptium\jdk-25…`.
Новое значение увидят только новые окна терминала и перезапущенная Android Studio.

### Если всё равно 21

| Что видно | Что сделать |
|---|---|
| `echo $env:JAVA_HOME` показывает старую JDK | Пуск → «Изменение системных переменных среды» → Переменные среды → `JAVA_HOME` = папка JDK 25 → OK. Открыть новый терминал |
| `java -version` показывает 26 или 21 | не страшно: `.\gradlew` берёт Java из `JAVA_HOME`, а не из `PATH`. Если мешает — поднять папку `…\jdk-25…\bin` выше в `Path` |
| в Android Studio сборка на 21 | шаг 3: *Gradle JDK* = 25 |
| в папке проекта есть `gradle\gradle-daemon-jvm.properties` с `toolchainVersion=21` | проект сам просит 21. В шаблоне KMP это не мешает; в Pixroost после шага 1.1 там будет 25 |

## Mac

### 1. Скачать

[adoptium.net → Temurin 25, macOS, JDK, `.pkg`](https://adoptium.net/temurin/releases/?version=25&os=mac&package=jdk):
для Mac на Apple Silicon (M1 и новее) — **aarch64**, для Mac на Intel — **x64**.
Какой у вас: меню Apple → Об этом Mac → строка «Чип».

### 2. Установить

Открыть `.pkg`, нажимать «Продолжить». JDK ляжет в
`/Library/Java/JavaVirtualMachines/temurin-25.jdk`. Путь не менять.

### 3. JAVA_HOME

В Терминале:

```bash
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 25)' >> ~/.zshrc
source ~/.zshrc
java -version            # openjdk version "25..."
echo $JAVA_HOME          # /Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home
```

### 4. Android Studio

Android Studio → Settings → Build, Execution, Deployment → Build Tools → **Gradle** → *Gradle JDK* →
**Eclipse Temurin 25**. OK, затем синхронизация Gradle.

Xcode своя JDK не нужна: Java нужна только Gradle, который собирает общий код для iOS.

## После шага 1.1

В Pixroost версию JDK зафиксирует сам проект: toolchain Gradle и файл `gradle/gradle-daemon-jvm.properties`
укажут 25, а если её нет — Gradle скачает её сам. Настройки выше останутся нужны для шаблона и для
случаев, когда Android Studio спрашивает, какую JDK взять.

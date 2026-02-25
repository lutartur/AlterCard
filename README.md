# AlterCard

Минималистичное Android-приложение для хранения карт лояльности и скидочных карт. Просто отсканируйте штрихкод карты — приложение сохранит его и отобразит в нужный момент на экране.

---

## Возможности

- **Хранение карт** — сохраняет название карты, номер и штрихкод в локальной базе данных
- **Сканер штрихкодов** — сканирование через камеру в реальном времени (CameraX + ML Kit), а также из файла галереи
- **Генерация штрихкодов** — отображает корректный штрихкод нужного формата на экране кассы (ZXing)
- **Ручной ввод** — добавление карты вручную без сканирования, с автоматической генерацией CODE-128
- **Настройка логотипа** — выбор цвета логотипа магазина в карточке
- **Синхронизация с Google Drive** — резервное копирование и синхронизация карт между устройствами через `appDataFolder` (приватное хранилище приложения)
- **Автосинхронизация** — автоматическая загрузка на Drive после каждого изменения

### Поддерживаемые форматы штрихкодов

QR Code, Code 128, Code 39, EAN-13, EAN-8, UPC-A, UPC-E, Data Matrix, Aztec, PDF417, ITF и другие форматы, распознаваемые ML Kit.

---

## Поддерживаемые языки интерфейса

| Язык       | Locale |
|------------|--------|
| Русский    | `ru`   |
| English    | —      |
| Deutsch    | `de`   |
| Français   | `fr`   |
| Italiano   | `it`   |
| Español    | `es`   |

Язык выбирается автоматически в соответствии с системными настройками устройства.

---

## Совместимость

| Параметр              | Значение              |
|-----------------------|-----------------------|
| Минимальная версия    | Android 12 (API 31)   |
| Целевая версия        | Android 14 (API 34)   |
| Compile SDK           | API 36                |
| Ориентация экрана     | Только портретная     |
| Версия приложения     | 1.0 (versionCode 1)   |

---

## Технический стек

### Инструменты сборки

| Инструмент                  | Версия  |
|-----------------------------|---------|
| Kotlin                      | 2.3.10  |
| Android Gradle Plugin (AGP) | 9.0.1   |
| KSP (Kotlin Symbol Processing) | 2.3.6 |
| Java compatibility          | Java 11 |

### Зависимости

| Библиотека                                | Версия                     | Назначение                          |
|-------------------------------------------|----------------------------|-------------------------------------|
| `androidx.core:core-ktx`                  | 1.17.0                     | Kotlin-расширения AndroidX          |
| `androidx.appcompat:appcompat`            | 1.7.1                      | Совместимость Activity/Fragment     |
| `com.google.android.material:material`    | 1.13.0                     | Material Design 3 компоненты        |
| `androidx.activity:activity-ktx`          | 1.12.4                     | Activity Result API                 |
| `androidx.coordinatorlayout`              | 1.3.0                      | CoordinatorLayout                   |
| `androidx.room:room-runtime`              | 2.8.4                      | Локальная база данных (Room/SQLite)  |
| `androidx.room:room-ktx`                  | 2.8.4                      | Корутины для Room                   |
| `androidx.lifecycle:lifecycle-viewmodel-ktx` | 2.10.0                  | ViewModel + Coroutines              |
| `androidx.lifecycle:lifecycle-livedata-ktx` | 2.10.0                   | LiveData + Coroutines               |
| `androidx.camera:camera-*`                | 1.5.3                      | CameraX (превью, анализ, lifecycle) |
| `com.google.mlkit:barcode-scanning`       | 17.3.0                     | Распознавание штрихкодов (ML Kit)   |
| `com.journeyapps:zxing-android-embedded`  | 4.3.0                      | Генерация штрихкодов (ZXing)        |
| `com.google.android.gms:play-services-auth` | 21.5.1                   | Google Sign-In                      |
| `com.google.api-client:google-api-client-android` | 2.7.0              | Google API клиент для Android       |
| `com.google.apis:google-api-services-drive` | v3-rev20240914-2.0.0    | Google Drive API v3                 |
| `com.google.http-client:google-http-client-gson` | 1.44.2              | HTTP клиент + GSON сериализация     |

---

## Структура проекта

```
altercard/
├── app/
│   └── src/main/
│       ├── java/com/altercard/
│       │   ├── AltercardApplication.kt     # Application-класс: инициализация DB и Repository
│       │   │
│       │   ├── # --- Data Layer ---
│       │   ├── Card.kt                     # Модель данных (Room Entity + поле lastModified)
│       │   ├── CardDao.kt                  # DAO: CRUD-операции с базой данных
│       │   ├── AppDatabase.kt              # Room-база данных (версия 3, миграции 1→2→3)
│       │   ├── CardRepository.kt           # Репозиторий: прослойка между DAO и ViewModel
│       │   │
│       │   ├── # --- ViewModel Layer ---
│       │   ├── CardViewModel.kt            # ViewModel: список карт, состояние синхронизации
│       │   │
│       │   ├── # --- UI Layer ---
│       │   ├── MainActivity.kt             # Список карт (RecyclerView + ExtendedFAB)
│       │   ├── AddCardActivity.kt          # Добавление карты: имя + номер + опциональный скан
│       │   ├── ScannerActivity.kt          # Сканер штрихкодов (CameraX + ML Kit)
│       │   ├── CardDetailActivity.kt       # Просмотр карты + отображение штрихкода
│       │   │
│       │   ├── # --- Custom Views ---
│       │   ├── CardAdapter.kt              # RecyclerView Adapter для списка карт
│       │   ├── ScannerOverlayView.kt       # Кастомный View: рамка сканера с затемнением
│       │   ├── ColorPickerView.kt          # Кастомный View: выбор цвета логотипа
│       │   │
│       │   └── # --- Google Drive Sync ---
│       │       ├── DriveAuthManager.kt     # Google Sign-In, получение credentials
│       │       ├── DriveRepository.kt      # Upload/download cards.json в appDataFolder
│       │       └── SyncManager.kt          # Логика синхронизации (авто + ручная, merge)
│       │
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml           # Разметка списка карт
│           │   ├── activity_add_card.xml        # Разметка экрана добавления
│           │   ├── activity_scanner.xml         # Разметка сканера
│           │   ├── activity_card_detail.xml     # Разметка детального просмотра
│           │   ├── dialog_support.xml           # Диалог поддержки проекта
│           │   └── list_item_card.xml           # Элемент списка карт
│           ├── values/                      # Ресурсы по умолчанию (EN)
│           ├── values-ru/                   # Русские строки
│           ├── values-de/                   # Немецкие строки
│           ├── values-fr/                   # Французские строки
│           ├── values-it/                   # Итальянские строки
│           ├── values-es/                   # Испанские строки
│           └── values-night/                # Цвета тёмной темы
├── build.gradle.kts                         # Корневой Gradle (версии плагинов)
├── settings.gradle.kts                      # Настройки проекта, репозитории
└── gradlew / gradlew.bat                    # Gradle Wrapper
```

---

## Модель данных

```kotlin
@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val number: String,
    val barcodeData: String?,       // Данные штрихкода (null = нет скана)
    val barcodeFormat: String?,     // Имя ZXing BarcodeFormat ("QR_CODE", "CODE_128", …)
    val lastModified: Long = 0L     // Unix-время последнего изменения (для merge-синхронизации)
)
```

База данных: Room SQLite, файл `altercard_database`, текущая версия схемы **3**.

---

## Сборка и запуск

### Требования

- **Android Studio** Ladybug (2024.2+) или новее
- **JDK 11+** (обычно входит в Android Studio)
- **Android SDK** с установленными API 31–36
- Для синхронизации с Google Drive: настроенный проект в Google Cloud Console (см. ниже)

### Клонирование и открытие

```bash
git clone <repo-url>
cd altercard
```

Откройте папку в Android Studio: **File → Open → выберите папку `altercard`**.

### Сборка через Android Studio

1. Дождитесь синхронизации Gradle (автоматически при открытии)
2. Выберите конфигурацию запуска **app**
3. Нажмите **Run ▶** или `Shift+F10`

### Сборка через командную строку (Gradle Wrapper)

```bash
# Синхронизация зависимостей и сборка debug APK
./gradlew assembleDebug

# Сборка release APK
./gradlew assembleRelease

# Установка debug APK на подключённое устройство/эмулятор
./gradlew installDebug

# Полная очистка build-артефактов
./gradlew clean

# Очистка + сборка debug
./gradlew clean assembleDebug
```

> **Windows:** используйте `gradlew.bat` вместо `./gradlew`
>
> ```bat
> gradlew.bat assembleDebug
> ```

### Расположение APK после сборки

```
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release-unsigned.apk
```

### Запуск через ADB

```bash
# Установка APK на устройство
adb install app/build/outputs/apk/debug/app-debug.apk

# Запуск приложения
adb shell am start -n com.altercard/.MainActivity

# Просмотр логов приложения
adb logcat -s AlterCard
```

---

## Настройка синхронизации с Google Drive

Для работы функции синхронизации необходимо настроить проект в Google Cloud Console:

1. Создайте проект на [console.cloud.google.com](https://console.cloud.google.com)
2. Включите **Google Drive API**
3. Создайте **OAuth 2.0 credentials** типа *Android*:
   - Package name: `com.altercard`
   - SHA-1 fingerprint: получить командой:
     ```bash
     # Debug keystore (для разработки)
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
4. Скачайте `google-services.json` и поместите в папку `app/`

> Синхронизация использует `appDataFolder` — приватное хранилище, невидимое пользователю в Google Drive. Данные доступны только через это приложение.

---

## Архитектура

Приложение построено по паттерну **MVVM** с однонаправленным потоком данных:

```
UI (Activity) ←→ ViewModel ←→ Repository ←→ Room DAO ←→ SQLite
                     ↕
               SyncManager ←→ DriveRepository ←→ Google Drive API
```

- **View Binding** — типобезопасный доступ к элементам разметки
- **LiveData / StateFlow** — реактивное обновление UI
- **Coroutines** — асинхронные операции (IO dispatcher)
- **KSP** — генерация кода Room во время компиляции (быстрее KAPT)

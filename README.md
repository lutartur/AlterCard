# AlterCard

Минималистичное Android-приложение для хранения карт лояльности и скидочных карт. Просто отсканируйте штрихкод карты — приложение сохранит его и отобразит в нужный момент на экране.

---

## Возможности

- **Хранение карт** — сохраняет название карты, номер и штрихкод в локальной базе данных
- **Сканер штрихкодов** — сканирование через камеру в реальном времени (CameraX + ML Kit), а также из файла галереи
- **Генерация штрихкодов** — отображает корректный штрихкод нужного формата на экране кассы (ZXing)
- **Ручной ввод** — добавление карты вручную без сканирования, с автоматической генерацией CODE-128
- **Настройка логотипа** — выбор цвета фона и цвета текста логотипа магазина в карточке (HSV-пикер)
- **Синхронизация с Google Drive** — резервное копирование и синхронизация карт между устройствами через `appDataFolder` (приватное хранилище приложения)
- **Автосинхронизация** — автоматическая загрузка на Drive после каждого изменения
- **Тёмная тема** — поддержка системной тёмной темы (Material Design 3 DayNight)

### Поддерживаемые форматы штрихкодов

QR Code, Code 128, Code 39, Code 93, CODABAR, EAN-13, EAN-8, UPC-A, UPC-E, Data Matrix, Aztec, PDF417, ITF и другие форматы, распознаваемые ML Kit.

---

## Поддерживаемые языки интерфейса

| Язык      | Locale |
|-----------|--------|
| English   | —      |
| Русский   | `ru`   |
| Deutsch   | `de`   |
| Français  | `fr`   |
| Italiano  | `it`   |
| Español   | `es`   |

Язык выбирается автоматически в соответствии с системными настройками устройства.

---

## Совместимость

| Параметр           | Значение            |
|--------------------|---------------------|
| Минимальная версия | Android 12 (API 31) |
| Целевая версия     | Android 15 (API 36) |
| Compile SDK        | API 36              |
| Ориентация экрана  | Только портретная   |
| Версия приложения  | 1.0 (versionCode 1) |

---

## Технический стек

### Инструменты сборки

| Инструмент                     | Версия |
|--------------------------------|--------|
| Kotlin                         | 2.3.10 |
| Android Gradle Plugin (AGP)    | 9.0.1  |
| KSP (Kotlin Symbol Processing) | 2.3.6  |
| Java compatibility             | 11     |

### Зависимости

| Библиотека                                        | Версия                | Назначение                          |
|---------------------------------------------------|-----------------------|-------------------------------------|
| `androidx.core:core-ktx`                          | 1.17.0                | Kotlin-расширения AndroidX          |
| `androidx.appcompat:appcompat`                    | 1.7.1                 | Совместимость Activity/Fragment     |
| `com.google.android.material:material`            | 1.13.0                | Material Design 3 компоненты        |
| `androidx.activity:activity-ktx`                  | 1.12.4                | Activity Result API                 |
| `androidx.coordinatorlayout:coordinatorlayout`    | 1.3.0                 | CoordinatorLayout                   |
| `androidx.room:room-runtime`                      | 2.8.4                 | Локальная база данных (Room/SQLite) |
| `androidx.room:room-ktx`                          | 2.8.4                 | Корутины для Room                   |
| `androidx.lifecycle:lifecycle-viewmodel-ktx`      | 2.10.0                | ViewModel + Coroutines              |
| `androidx.lifecycle:lifecycle-livedata-ktx`       | 2.10.0                | LiveData + Coroutines               |
| `androidx.camera:camera-*`                        | 1.5.3                 | CameraX (превью, анализ, lifecycle) |
| `com.google.mlkit:barcode-scanning`               | 17.3.0                | Распознавание штрихкодов            |
| `com.journeyapps:zxing-android-embedded`          | 4.3.0                 | Генерация штрихкодов                |
| `com.google.android.gms:play-services-auth`       | 21.5.1                | Google Sign-In                      |
| `com.google.api-client:google-api-client-android` | 2.7.0                 | Google API клиент для Android       |
| `com.google.apis:google-api-services-drive`       | v3-rev20240914-2.0.0 | Google Drive API v3                 |
| `com.google.http-client:google-http-client-gson`  | 1.44.2                | HTTP клиент + GSON сериализация     |

---

## Структура проекта

```
altercard/
├── app/
│   └── src/main/
│       ├── java/com/altercard/
│       │   ├── AltercardApplication.kt     # Application-класс: инициализация DB, Repository, SyncManager
│       │   │
│       │   ├── # --- Data Layer ---
│       │   ├── Card.kt                     # Room Entity (id, name, number, barcodeData, barcodeFormat,
│       │   │                               #   customBackgroundColor, customTextColor, lastModified, isDeleted)
│       │   ├── CardDao.kt                  # DAO: CRUD-операции с базой данных
│       │   ├── AppDatabase.kt              # Room база данных (версия 4, миграции 1→2→3→4)
│       │   ├── CardRepository.kt           # Репозиторий: прослойка между DAO и ViewModel
│       │   │
│       │   ├── # --- ViewModel Layer ---
│       │   ├── CardViewModel.kt            # ViewModel: список карт, состояния синхронизации (SyncState)
│       │   │
│       │   ├── # --- UI Layer ---
│       │   ├── MainActivity.kt             # Список карт (RecyclerView + ExtendedFAB + диалог поддержки)
│       │   ├── AddCardActivity.kt          # Добавление карты: имя + номер + опциональный скан
│       │   ├── ScannerActivity.kt          # Сканер штрихкодов (CameraX + ML Kit)
│       │   ├── CardDetailActivity.kt       # Просмотр карты + штрихкод + настройки
│       │   │
│       │   ├── # --- Custom Views ---
│       │   ├── CardAdapter.kt              # RecyclerView ListAdapter с DiffUtil
│       │   ├── ScannerOverlayView.kt       # Рамка сканера с затемнением и угловыми маркерами
│       │   ├── ColorPickerView.kt          # HSV-пикер цвета (три слайдера + превью)
│       │   │
│       │   └── # --- Google Drive Sync ---
│       │       ├── DriveAuthManager.kt     # Google Sign-In, OAuth credentials (scope: DRIVE_APPDATA)
│       │       ├── DriveRepository.kt      # Upload/download cards.json в appDataFolder (GSON)
│       │       └── SyncManager.kt          # Логика синхронизации: авто, ручная, merge по lastModified
│       │
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml           # Список карт
│           │   ├── activity_add_card.xml        # Экран добавления карты
│           │   ├── activity_scanner.xml         # Экран сканера
│           │   ├── activity_card_detail.xml     # Детальный просмотр карты
│           │   ├── dialog_support.xml           # Диалог поддержки проекта
│           │   └── list_item_card.xml           # Элемент списка карт
│           ├── values/                          # Ресурсы по умолчанию (EN)
│           ├── values-ru/                       # Русские строки
│           ├── values-de/                       # Немецкие строки
│           ├── values-fr/                       # Французские строки
│           ├── values-it/                       # Итальянские строки
│           ├── values-es/                       # Испанские строки
│           └── values-night/                    # Цвета тёмной темы
├── app/proguard-rules.pro               # Правила R8/ProGuard для release-сборки
├── keystore.properties.example          # Шаблон конфигурации подписи APK
├── build.gradle.kts                     # Корневой Gradle (версии плагинов)
└── settings.gradle.kts                  # Настройки проекта и репозиториев
```

---

## Модель данных

```kotlin
@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val number: String,
    val barcodeData: String? = null,           // Данные штрихкода (null = нет скана)
    val barcodeFormat: String? = null,         // Имя ZXing BarcodeFormat ("QR_CODE", "CODE_128", …)
    val customBackgroundColor: Int? = null,    // Кастомный цвет фона аватара (ARGB Int)
    val customTextColor: Int? = null,          // Кастомный цвет текста аватара (ARGB Int)
    val lastModified: Long = 0L,               // Unix-время изменения (для merge-синхронизации)
    val isDeleted: Boolean = false             // Soft-delete флаг (для синхронизации между устройствами)
)
```

База данных: Room SQLite, файл `altercard_database`, текущая версия схемы **4**.

---

## Сборка и запуск

### Требования

- **Android Studio** Ladybug (2024.2) или новее
- **JDK 11+** (входит в Android Studio)
- **Android SDK** с установленными API 31–36
- Для Google Drive синхронизации: настроенный проект в Google Cloud Console (см. ниже)

### Клонирование и открытие

```bash
git clone <repo-url>
cd altercard
```

Откройте папку в Android Studio: **File → Open → выберите папку `altercard`**.

### Debug-сборка

```bash
# Сборка debug APK
./gradlew assembleDebug

# Установка на подключённое устройство/эмулятор
./gradlew installDebug

# Очистка артефактов сборки
./gradlew clean
```

> **Windows:** используйте `gradlew.bat` вместо `./gradlew`

### Release-сборка (для Google Play)

1. Создайте keystore (однократно):
   ```bash
   keytool -genkey -v -keystore altercard.keystore -alias altercard -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Создайте файл `keystore.properties` в корне проекта (по образцу `keystore.properties.example`):
   ```properties
   storeFile=../altercard.keystore
   storePassword=YOUR_STORE_PASSWORD
   keyAlias=YOUR_KEY_ALIAS
   keyPassword=YOUR_KEY_PASSWORD
   ```
   > Файл добавлен в `.gitignore` — не коммитьте его в репозиторий.

3. Соберите подписанный AAB для Google Play:
   ```bash
   ./gradlew bundleRelease
   ```
   Готовый файл: `app/build/outputs/bundle/release/app-release.aab`

4. Или соберите подписанный APK:
   ```bash
   ./gradlew assembleRelease
   ```
   Готовый файл: `app/build/outputs/apk/release/app-release.apk`

> В release-сборке включены R8/ProGuard минификация и удаление неиспользуемых ресурсов (`isShrinkResources = true`).

### Запуск через ADB

```bash
# Установка APK на устройство
adb install app/build/outputs/apk/debug/app-debug.apk

# Запуск приложения
adb shell am start -n com.altercard/.MainActivity
```

---

## Настройка синхронизации с Google Drive

Для работы функции синхронизации необходимо настроить проект в Google Cloud Console:

1. Создайте проект на [console.cloud.google.com](https://console.cloud.google.com)
2. Включите **Google Drive API**
3. Создайте **OAuth 2.0 credentials** типа *Android*:
   - Package name: `com.altercard`
   - SHA-1 fingerprint вашего keystore:
     ```bash
     # Debug keystore
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

     # Release keystore
     keytool -list -v -keystore altercard.keystore -alias altercard
     ```

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
- **R8/ProGuard** — минификация и обфускация в release-сборке

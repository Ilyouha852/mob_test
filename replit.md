# MOBDEV_LAB3 - Android Application

## Project Overview
This is an Android mobile application (Lab 3 / Lab 4) for mobile development coursework. It demonstrates file management, data storage strategies, and asynchronous programming in Android.

## Tech Stack
- **Language:** Kotlin (1.8.22) with some generated Java (greenDAO)
- **UI:** Android XML layouts with Material Design components
- **Build System:** Gradle 7.6.3 with Kotlin DSL (`.gradle.kts`)
- **Min SDK:** 24 (Android 7.0), Target SDK: 33 (Android 13)
- **Database:** greenDAO ORM for SQLite
- **Async:** Kotlin Coroutines + Java Threads

## Key Features
- File Manager with sorting (name, type, date) and bookmarking
- Data Storage Lab: SharedPreferences (JSON/Gson), TXT files, CSV files (MediaStore API)
- Concurrency Lab: Java Threads, GlobalScope coroutines, lifecycleScope coroutines
- SQLite database management via greenDAO
- Network Lab: Retrofit + OkHttp HTTP/2 client fetching user data from JSONPlaceholder API

## Project Structure
```
app/src/main/java/com/example/mobdev_lab3/
├── adapter/       - RecyclerView and ListView adapters
├── database/      - greenDAO config, DAOs, entities, repository
├── helper/        - UI and logic helpers
├── manager/       - Business logic (BookmarksManager)
├── model/         - Data models (Note, FileItem, FileBookmark)
├── network/       - Retrofit/OkHttp networking (ApiService, NetworkClient, NetworkRepository)
├── presentation/  - Fragments and ViewModels organized by feature
├── repository/    - Data access layer (CSV, TXT, SharedPreferences)
├── ui/storage/    - Storage lab fragments
└── viewmodel/     - FileManagerViewModel
```

## Replit Environment Setup
- **Java:** OpenJDK 17.0.15+6 (set via `org.gradle.java.home` in gradle.properties)
  - Path: `/nix/store/bk2hgshkd3a9v4hrs9gjmxfkzvflgydx-openjdk-17.0.15+6`
  - Note: GraalVM JDK 19 (default) is incompatible with Android build tools (missing jlink support)
- **Android SDK:** Downloaded and installed at `/home/runner/android-sdk`
  - Platform: android-33
  - Build Tools: 33.0.2
  - Platform Tools included
  - Command line tools: cmdline-tools/latest
- **Workflow:** "Build Android App" - runs `./gradlew assembleDebug`
- **local.properties:** Points to `sdk.dir=/home/runner/android-sdk`

## Building
The workflow runs `./gradlew assembleDebug` which compiles the app.
The APK output will be at: `app/build/outputs/apk/debug/app-debug.apk`

**Note:** This is an Android mobile app — it cannot be previewed in a web browser. 
It must be installed on an Android device or emulator to run.

## Bug Fixes Applied
- **StorageFragment.kt**: Fixed wrong import — `FragmentStateAdapter` was imported from
  `androidx.fragment.app` (wrong) instead of `androidx.viewpager2.adapter` (correct).
- **app/build.gradle.kts**: Added missing `androidx.viewpager2:viewpager2:1.1.0-beta02` dependency.

## Network Module (Лаб. работа — Сетевые запросы)

### Архитектура
```
network/
├── model/
│   ├── User.kt       — модель пользователя (десериализация из JSON)
│   └── Post.kt       — модель поста
├── ApiService.kt     — Retrofit-интерфейс (GET /users/{id}, GET /posts?userId=)
├── NetworkClient.kt  — OkHttp + Retrofit singleton (HTTP/2, кэш, таймауты, логирование)
└── NetworkRepository.kt — sealed class NetworkResult<T>, корутины, обработка ошибок
NetworkLabActivity.kt — UI лабораторной работы
```

### Ключевые возможности
- **HTTP/2** через `Protocol.HTTP_2` в OkHttp (с fallback на HTTP/1.1)
- **Кэширование** — OkHttp Cache 10 МБ, `max-age=60` сек через NetworkInterceptor
- **Таймауты** — connect 15 с, read 20 с, write 20 с
- **Отмена запросов** — через `Job.cancel()` корутины (CancellationException)
- **Десериализация** — GSON + `@SerializedName` аннотации
- **2 связанных запроса**: GET `/users/{id}` → userId → GET `/posts?userId={id}`
- **Обработка ошибок** — sealed NetworkResult (Success/Error/Cancelled)
- **API**: JSONPlaceholder (https://jsonplaceholder.typicode.com)

## Dependencies (key)
- `com.squareup.okhttp3:okhttp:4.11.0` - HTTP/2 клиент с кэшированием и таймаутами
- `com.squareup.okhttp3:logging-interceptor:4.11.0` - логирование HTTP заголовков
- `com.squareup.retrofit2:retrofit:2.9.0` - типобезопасный REST клиент
- `com.squareup.retrofit2:converter-gson:2.9.0` - конвертер JSON→объекты
- `com.google.code.gson:gson:2.10.1` - JSON serialization
- `org.greenrobot:greendao:3.3.0` - SQLite ORM
- `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3` - Coroutines
- `androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2` - ViewModel
- `androidx.viewpager2:viewpager2:1.1.0-beta02` - ViewPager2 for StorageFragment
- `com.google.android.material:material:1.8.0` - Material Design UI

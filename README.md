# CumberMovies (MVVM + MVI + Paging 3)

This is a small movie browser that lists **Benedict Cumberbatch** movies using the **TMDB API**.  
It demonstrates two common Android architectures **side-by-side**:

- **MVVM** – Movie list screen using **XML + RecyclerView** and **Paging 3**
- **MVI** – Movie detail screen using **Jetpack Compose**

It’s intentionally hybrid to show how to bridge legacy XML screens with new Compose screens (a
real-world scenario).

---

## 🎯 Features

- Paginated list of Benedict Cumberbatch movies (`with_people=71580`)
- Detail screen with poster, title, and overview
- **Load state** + error handling with retry
- **Hilt** for dependency injection
- **Retrofit + Kotlinx Serialization**
- **Coil** image loading
- Example **unit tests**
- Optional **debug delay** (`DEMO_DELAY_MS`) to show spinners per page

---

## 🛠️ Setup

1. **Get a TMDB API key** (free) from https://developer.themoviedb.org/.
2. Put it in your **`local.properties`** (this file is not committed):
   ```properties
   TMDB_API_KEY=your_tmdb_api_key_here
3. Expose the key to code via BuildConfig:
    ```properties
   defaultConfig {
   val tmdbApiKey: String = project.findProperty("TMDB_API_KEY") as? String ?: ""
   buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
   }
4. Open the project in Android Studio (minSdk 24) and Run the app.

## 🧩 Architecture

**Clean layered design (hybrid MVVM + MVI)**

### Data layer

- `MovieApiService` (Retrofit + `kotlinx.serialization`) defines TMDB endpoints.
- `MoviePagingSource` (Paging 3) loads pages and uses `total_pages` to detect end-of-list.
- `MovieRepository`
    - **List:** returns `Flow<PagingData<MovieDto>>` (UI uses Paging `LoadState` for loading/error).
    - **Detail:** wraps calls in `safeApiCall` and returns `DataState<MovieDto>` with friendly
      errors.

### Domain layer

- `Movie` (domain model) with null-safe defaults for UI.
- `GetMoviesUseCase`
    - **List:** maps `MovieDto → Movie` and exposes `Flow<PagingData<Movie>>`.
    - **Detail:** maps to `Flow<DataState<Movie>>` emitting **Loading → Success/Error**.

### Presentation layer

- **MVVM (List):** XML + RecyclerView + Paging adapters. `MovieListViewModel` exposes
  `Flow<PagingData<Movie>>`; UI observes `LoadState` for spinner/retry.
- **MVI (Detail):** Compose screen with intents (`MovieDetailIntent`) → `MovieDetailViewModel` →
  immutable `MovieDetailState` (`isLoading`, `movie`, `error`).

#### Why hybrid?

Paging integrates naturally with MVVM & RecyclerView; a single-item fetch (detail) fits the MVI
“intent → state” flow. This mirrors real projects migrating from XML to Compose.

## 📚 Libraries Used (and why)

| Library                                             | Purpose / Why                                       |
|-----------------------------------------------------|-----------------------------------------------------|
| **Retrofit + OkHttp**                               | Reliable networking with coroutine support          |
| **kotlinx.serialization**                           | Fast, reflection-free JSON on Android               |
| **Paging 3**                                        | Efficient infinite scroll with built-in load states |
| **Hilt (KSP)**                                      | Simple dependency injection and scoping             |
| **Jetpack Compose**                                 | Declarative UI for the detail screen (MVI)          |
| **RecyclerView**                                    | Mature list UI for MVVM list screen                 |
| **Coil**                                            | Lightweight image loading (Compose & XML)           |
| **Coroutines + Flow**                               | Structured concurrency & reactive streams           |
| **JUnit / MockK / coroutines-test / paging-common** | Fast JVM unit testing                               |

---

## 📂 Project Structure

````
    app/
    ├─ data/
    │ ├─ model/ # MovieDto, MovieResponse (kotlinx.serialization)
    │ ├─ remote/ # Retrofit API, MoviePagingSource
    │ └─ repository/ # MovieRepository, safeApiCall
    ├─ domain/
    │ ├─ model/ # Movie (domain model)
    │ ├─ common/ # DataState (Loading/Success/Error)
    │ └─ usecase/ # GetMoviesUseCase
    └─ presentation/
    ├─ mvvm/ # Movie list (XML + RecyclerView + Paging)
    └─ mvi/ # Movie detail (Compose + Intents + State)
````

## 🧠 Error Handling

All API calls are wrapped in a unified `safeApiCall`, mapping exceptions to readable messages via
`DataState.Error`:

```
- `UnknownHostException` → **“No internet connection.”**
- `IOException` → **“Network error. Please try again.”**
- `HttpException`
    - `401/403` → **“You are not authorized.”**
    - `404` → **“Requested resource not found.”**
    - `429` → **“Too many requests, please retry later.”**
    - `5xx` → **“Server unreachable.”**
- `SerializationException` → **“Couldn’t read the server response.”**
```
### UI consumption

- **List:** shows `LoadState.Error` from Paging (retry button in footer).
- **Detail:** observes `Flow<DataState<Movie>>` to render loading, error, or content.

---

## 🔁 Paging Behavior

- **Page size:** 5 (kept small so loading is visible)
- **LoadState footer:** spinner + retry
- **End condition:** stops when `page >= total_pages`
- **Demo delay:**
  ```kotlin
  // MoviePagingSource.load(...)
  delay(1000L) // demo-only; remove or set to 0 for production

---

## 💡 What Could Be Improved

- Offline cache with **Room** + `RemoteMediator`
- Migrate list screen to **Compose**
- **Shimmer** placeholders, empty states, better theming (dark mode)
- **CI/CD** (GitHub Actions) for build, tests, lint
- **Localization** for messages and UI strings
- **Accessibility** polish (content descriptions, contrast, larger targets)

---

## 🚧 Challenges Encountered

- **Paging tests** are heavy; kept to **smoke tests** with `PagingData.from(...)`.
- **Coroutine timing** in tests; used test dispatchers + terminal state collection.
- Centralized **error mapping** simplified VM logic and UI updates.
- Maintaining **XML + Compose** side-by-side to reflect real migration paths.

## 🧪 Testing

**Covered areas**

- **Repository (detail):** `safeApiCall` maps success & common failures to `DataState`.
- **Use case:** emits **Loading → Success** or **Loading → Error** and maps **DTO → domain**.
- **ViewModels:**
    - **Detail (MVI):** `LoadMovie` intent updates `MovieDetailState` accordingly.
    - **List (MVVM):** exposes `Flow<PagingData<Movie>>` (smoke test using `PagingData.from(...)`).

```bash
./gradlew testDebugUnitTest
```

## 🙌 Credits

- Data: **TMDB (The Movie Database)**
- Author: **Divya Deep Dalotra** — Kotlin • Jetpack Compose • Clean Architecture




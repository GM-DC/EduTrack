# Plan: Arquitectura Clean Architecture — EduTrack (Detallado)

Estructurar `org.owlcode.edutrack` en cinco capas bajo `webMain`, usando **Koin** para DI, **Ktor** para consumo de REST API, e **IndexedDB** (async con coroutines) para persistencia local. La navegación se gestionará con **Compose Navigation multiplatform**.

---

## Paso 1 — Agregar dependencias en `libs.versions.toml` y `build.gradle.kts`

Agregar las siguientes librerías:

| Librería | Propósito |
|---|---|
| `koin-core` + `koin-compose` | Inyección de dependencias |
| `ktor-client-core` + `ktor-client-js` | Cliente HTTP REST (target JS/WASM) |
| `ktor-client-content-negotiation` + `ktor-serialization-kotlinx-json` | Serialización JSON con Ktor |
| `kotlinx-serialization-json` | Serialización de modelos |
| `androidx.navigation:navigation-compose` | Navegación entre features |
| `kotlinx-browser` | Acceso a Web APIs (IndexedDB) en JS/WASM |

También agregar el plugin `kotlinx.serialization` en el bloque `plugins` del `build.gradle.kts`.

---

## Paso 2 — Crear `core/`

Utilidades transversales que no pertenecen a ninguna capa de negocio:

```
core/
├── di/
│   └── CoreModule.kt         → módulo Koin con HttpClient, DatabaseDriver
├── network/
│   └── HttpClientFactory.kt  → configura Ktor con JSON + manejo de errores
├── database/
│   └── IndexedDbDriver.kt    → wrapper async (coroutines) sobre IndexedDB Web API
└── result/
    ├── AppError.kt           → sealed class con tipos de error (Network, Local, Unknown)
    └── AppResult.kt          → typealias de Result<T> con extensiones útiles
```

`IndexedDbDriver` usará la API de `kotlinx-browser` para envolver las operaciones de IndexedDB en `suspendCoroutine`, exponiendo métodos `suspend fun get/put/delete`.

---

## Paso 3 — Crear `domain/`

Capa pura de negocio, **sin dependencias de framework**:

```
domain/
├── model/
│   ├── User.kt               → data class (id, name, email, role)
│   ├── CalendarEvent.kt      → data class (id, title, date, description)
│   └── Assignment.kt         → data class (id, title, dueDate, status)
└── repository/
    ├── AuthRepository.kt     → interface: suspend fun login/logout/currentUser
    └── CalendarRepository.kt → interface: suspend fun getEvents/addEvent/deleteEvent
```

---

## Paso 4 — Crear `data/`

Implementaciones concretas de las interfaces de dominio:

```
data/
├── local/
│   ├── datasource/
│   │   ├── AuthLocalDataSource.kt      → guarda sesión en IndexedDB
│   │   └── CalendarLocalDataSource.kt  → CRUD de eventos en IndexedDB
│   └── mapper/
│       └── LocalMappers.kt             → entidades de dominio ↔ formato IndexedDB
├── remote/
│   ├── api/
│   │   ├── AuthApiService.kt           → llamadas Ktor: POST /auth/login, /logout
│   │   └── CalendarApiService.kt       → llamadas Ktor: GET/POST/DELETE /events
│   ├── dto/
│   │   ├── UserDto.kt                  → @Serializable data class
│   │   └── CalendarEventDto.kt         → @Serializable data class
│   └── mapper/
│       └── RemoteMappers.kt            → DTOs ↔ modelos de dominio
└── repository/
    ├── AuthRepositoryImpl.kt           → implementa AuthRepository
    └── CalendarRepositoryImpl.kt       → implementa CalendarRepository
```

Cada `RepositoryImpl` aplica estrategia **offline-first**: lee de local, actualiza desde remoto y persiste localmente.

---

## Paso 5 — Crear `sync/`

Capa de coordinación de sincronización entre local y remoto:

```
sync/
├── SyncManager.kt     → orquesta sincronización; expone suspend fun syncAll()
└── SyncState.kt       → sealed class: Idle, Syncing, Success, Error
```

`SyncManager` se invoca al iniciar la app (desde `AppViewModel`) y cuando hay conectividad disponible, usando un `Flow` de estado para que la UI pueda reaccionar.

---

## Paso 6 — Reorganizar y completar `features/`

Cada feature sigue el patrón **Screen + ViewModel + State + Koin module**:

```
features/
├── app/
│   ├── App.kt               → NavHost con rutas definidas en un sealed class Route
│   ├── AppViewModel.kt      → inicia SyncManager, controla sesión global
│   └── di/
│       └── AppModule.kt     → Koin: agrupa todos los sub-módulos
├── login/
│   ├── LoginScreen.kt       → Composable con campos email/password
│   ├── LoginViewModel.kt    → llama AuthRepository, emite LoginState
│   ├── LoginState.kt        → sealed class: Idle, Loading, Success, Error
│   └── di/
│       └── LoginModule.kt   → Koin: provee LoginViewModel
└── calendar/
    ├── CalendarScreen.kt    → Composable que muestra lista de eventos
    ├── CalendarViewModel.kt → llama CalendarRepository, emite CalendarState
    ├── CalendarState.kt     → sealed class: Loading, Content(events), Error
    └── di/
        └── CalendarModule.kt → Koin: provee CalendarViewModel
```

En `main.kt` se inicializa Koin con `startKoin { modules(AppModule) }` antes de lanzar `ComposeViewport`.

---

## Estructura Final de Carpetas

```
composeApp/src/webMain/kotlin/org/owlcode/edutrack/
│
├── core/
│   ├── di/
│   │   └── CoreModule.kt
│   ├── network/
│   │   └── HttpClientFactory.kt
│   ├── database/
│   │   └── IndexedDbDriver.kt
│   └── result/
│       ├── AppError.kt
│       └── AppResult.kt
│
├── domain/
│   ├── model/
│   │   ├── User.kt
│   │   ├── CalendarEvent.kt
│   │   └── Assignment.kt
│   └── repository/
│       ├── AuthRepository.kt
│       └── CalendarRepository.kt
│
├── data/
│   ├── local/
│   │   ├── datasource/
│   │   │   ├── AuthLocalDataSource.kt
│   │   │   └── CalendarLocalDataSource.kt
│   │   └── mapper/
│   │       └── LocalMappers.kt
│   ├── remote/
│   │   ├── api/
│   │   │   ├── AuthApiService.kt
│   │   │   └── CalendarApiService.kt
│   │   ├── dto/
│   │   │   ├── UserDto.kt
│   │   │   └── CalendarEventDto.kt
│   │   └── mapper/
│   │       └── RemoteMappers.kt
│   └── repository/
│       ├── AuthRepositoryImpl.kt
│       └── CalendarRepositoryImpl.kt
│
├── sync/
│   ├── SyncManager.kt
│   └── SyncState.kt
│
├── features/
│   ├── app/
│   │   ├── App.kt
│   │   ├── AppViewModel.kt
│   │   └── di/
│   │       └── AppModule.kt
│   ├── login/
│   │   ├── LoginScreen.kt
│   │   ├── LoginViewModel.kt
│   │   ├── LoginState.kt
│   │   └── di/
│   │       └── LoginModule.kt
│   └── calendar/
│       ├── CalendarScreen.kt
│       ├── CalendarViewModel.kt
│       ├── CalendarState.kt
│       └── di/
│           └── CalendarModule.kt
│
└── main.kt   ← startKoin + ComposeViewport
```

---

## Consideraciones Adicionales

1. **Autenticación con la API REST:** Define si usará **JWT** (token en header) o **session cookies**. Esto determina cómo `HttpClientFactory` configura el plugin `Auth` de Ktor y cómo `AuthLocalDataSource` guarda el token en IndexedDB.
2. **Archivos `Greeting.kt` y `Platform.kt`:** Son scaffolding inicial; pueden eliminarse o mantenerse temporalmente hasta que `LoginScreen` reemplace a `App.kt` como punto de entrada de la UI.
3. **Patrón de navegación:** Se recomienda un `sealed class Route` con objetos para cada destino (`Login`, `Calendar`), centralizado en `features/app/`, para evitar strings sueltos en el `NavHost`.



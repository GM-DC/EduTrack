# 🗺️ Arquitectura EduTrack — Roadmap

## Stack Tecnológico
| Capa | Tecnología |
|---|---|
| UI | Compose Multiplatform (Material 3) |
| Navegación | Navigation Compose 2.9 (type-safe routes) |
| ViewModel | androidx.lifecycle:lifecycle-viewmodel-compose |
| DI | Koin 4.1 |
| Red | Ktor Client 3.1 (JS/WasmJS) |
| Serialización | kotlinx.serialization |
| Fechas | kotlinx-datetime 0.7.1 |
| Targets | JS Browser + WasmJS Browser |

---

## Estructura de Capas

```
composeApp/src/webMain/kotlin/org/owlcode/edutrack/
│
├── main.kt                        ← Punto de entrada (startKoin + renderComposable)
├── App.kt                         ← Root Composable + NavHost
│
├── core/                          ← Infraestructura transversal
│   ├── di/   CoreModule.kt        ← Módulo Koin principal (red, base de datos)
│   ├── network/                   ← Configuración Ktor HttpClient
│   ├── database/                  ← Persistencia local (localStorage / IndexedDB)
│   └── result/                    ← Sealed class Result<T> para manejo de errores
│
├── domain/                        ← Reglas de negocio puras (sin Android/Web)
│   ├── model/                     ← Entidades del dominio
│   │   ├── User, Course, Clase, Tarea, Examen
│   │   ├── CalendarEvent, CalendarItem, EventoPersonal
│   │   ├── RecurrenceRule, Assignment, Enums
│   ├── repository/                ← Interfaces de repositorios (contratos)
│   │   ├── AuthRepository, CourseRepository, CalendarRepository
│   │   ├── ClaseRepository, TareaRepository, ExamenRepository
│   │   └── PersonalRepository
│   └── engine/
│       └── RecurrenceEngine.kt    ← Lógica de recurrencias de eventos
│
├── data/                          ← Implementaciones de repositorios
│   ├── remote/
│   │   ├── api/                   ← Llamadas Ktor (endpoints)
│   │   ├── dto/                   ← Data Transfer Objects (@Serializable)
│   │   └── mapper/                ← DTO → Modelo de dominio
│   ├── local/                     ← Fuentes de datos locales
│   ├── repository/                ← Implementaciones concretas + Mocks
│   │   ├── AuthRepositoryImpl, CalendarRepositoryImpl, CourseRepositoryImpl
│   │   ├── MockAuthRepository, MockCalendarRepository, MockCourseRepository
│   │   └── … (Clase, Examen, Tarea, Personal)
│   └── di/                        ← Módulo Koin de datos
│
├── sync/
│   ├── SyncManager.kt             ← Orquesta sincronización local ↔ remota
│   └── SyncState.kt               ← Estado de sincronización
│
└── features/                      ← Módulos de pantalla (MVVM)
    ├── app/
    │   ├── AppViewModel.kt        ← Estado global: isAuthenticated, isInitialized
    │   ├── Route.kt               ← Rutas de navegación selladas + type-safe
    │   └── di/
    ├── login/
    │   ├── LoginScreen.kt
    │   ├── LoginViewModel.kt
    │   ├── LoginState.kt
    │   └── di/
    ├── calendar/
    │   ├── CalendarScreen.kt
    │   ├── CalendarViewModel.kt
    │   ├── CalendarState.kt
    │   ├── CalendarViewMode.kt
    │   ├── components/            ← Componentes UI reutilizables del calendario
    │   ├── day/                   ← Vista de día
    │   ├── week/                  ← Vista de semana
    │   ├── month/                 ← Vista de mes
    │   ├── utils/
    │   └── di/
    └── courses/
        ├── CourseScreen.kt
        ├── CourseViewModel.kt
        ├── CourseUiState.kt
        ├── CourseDetailScreen.kt
        ├── CourseDetailViewModel.kt
        ├── CourseDetailUiState.kt
        ├── components/
        └── di/
```

---

## Flujo de Navegación

```
App.kt
  │
  ├─ isInitialized = false → CircularProgressIndicator (splash)
  │
  └─ isInitialized = true
       ├─ isAuthenticated = false → LoginScreen
       │       └─ onLoginSuccess → CalendarScreen (popUpTo Login)
       └─ isAuthenticated = true  → CalendarScreen
               ├─ onLogout        → LoginScreen (popUpTo Calendar)
               └─ onNavigateToCourses → CourseScreen
                       └─ onNavigateToCourseDetail(id) → CourseDetailScreen
```

---

## Flujo de Datos (por feature)

```
UI (Screen) ←→ ViewModel ←→ Repository (interfaz domain)
                                  ↓
                       RepositoryImpl (data)
                          ├─ RemoteDataSource (Ktor API)
                          └─ LocalDataSource  (storage)
                                  ↓
                           SyncManager (reconcilia ambas fuentes)
```

---

## Issues Conocidos / Pendientes

| # | Problema | Estado |
|---|---|---|
| 1 | Pantalla en blanco al iniciar | ⚠️ `isInitialized` debe resolverse antes de renderizar NavHost |
| 2 | Login no se muestra al inicio | 🔍 Verificar que `AppViewModel` emite `isInitialized=true` correctamente |
| 3 | Campos de fecha/hora sin placeholder | 🔲 Agregar hint con formato (ej: `DD/MM/AAAA HH:mm`) |
| 4 | Mocks activos en producción | 🔲 Separar `MockRepository` con flag de entorno |

---

## Consideraciones Finales

1. **Pantalla en blanco**: El `AppViewModel.isInitialized` podría no estar emitiendo `true` — revisar que el `StateFlow` tenga valor inicial correcto y que `checkSession()` se llame en `init {}`.
2. **Mocks vs Real API**: Actualmente existen `MockAuthRepository` y `MockCalendarRepository` — definir en el módulo Koin cuándo usar mocks vs implementación real (por ejemplo, con una constante `BuildConfig.USE_MOCK`).
3. **Placeholders de fecha**: Se recomienda crear un `DateTimeFormatter` en `core/` con el formato estándar del proyecto (`DD/MM/AAAA HH:mm`) y usarlo en todos los campos de texto de fecha/hora como `placeholder`.


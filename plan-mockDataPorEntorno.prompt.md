# Plan: Data Mockeada en localhost, Real en Producción

Agregar una bandera `useMockData` en `AppConfig` que detecta si la URL es `localhost`, y usarla en `DataModule` para inyectar repositorios mock o reales según el entorno, sin tocar el código de features ni requerir backend local.

---

## Pasos

### 1. Agregar `useMockData` en `build.gradle.kts`

Dentro de la tarea `generateAppConfig`, agregar la siguiente propiedad al objeto `AppConfig` generado:

```kotlin
/** True cuando la API apunta a localhost → usa datos mockeados */
val useMockData: Boolean get() = API_BASE_URL.contains("localhost")
```

Archivo: `composeApp/build.gradle.kts`

---

### 2. Crear `MockClaseRepository`

Archivo: `composeApp/src/webMain/kotlin/org/owlcode/edutrack/data/repository/MockClaseRepository.kt`

Implementar `ClaseRepository` con datos en memoria coherentes con los cursos mock (`c1`–`c4`). Ejemplo de datos:

```kotlin
class MockClaseRepository : ClaseRepository {
    private val clases = mutableListOf(
        Clase("cl1", "c1", "Límites y Continuidad",   "2026-04-28", "08:00", "10:00", "Sala B-302"),
        Clase("cl2", "c1", "Derivadas",                "2026-05-05", "08:00", "10:00", "Sala B-302"),
        Clase("cl3", "c2", "Movimiento Rectilíneo",    "2026-04-29", "10:00", "12:00", "Aula Magna"),
        Clase("cl4", "c3", "Búsqueda y Ordenamiento",  "2026-04-30", "14:00", "16:00", "Lab Cómputo 201"),
        Clase("cl5", "c4", "Enlace Covalente",         "2026-05-02", "08:00", "10:00", "Lab Química P2"),
    )
    private var nextId = 6
    // CRUD completo en memoria
}
```

---

### 3. Crear `MockTareaRepository`

Archivo: `composeApp/src/webMain/kotlin/org/owlcode/edutrack/data/repository/MockTareaRepository.kt`

Implementar `TareaRepository` con datos en memoria:

```kotlin
class MockTareaRepository : TareaRepository {
    private val tareas = mutableListOf(
        Tarea("t1", "c1", "Ejercicios Cap. 3",    "Resolver 20 ejercicios", TaskPriority.HIGH,   dueDate = "2026-05-02"),
        Tarea("t2", "c1", "Tarea: Integrales",    "",                       TaskPriority.MEDIUM, dueDate = "2026-05-07"),
        Tarea("t3", "c2", "Informe de laboratorio","Redactar resultados",   TaskPriority.HIGH,   dueDate = "2026-04-30"),
        Tarea("t4", "c3", "Proyecto Final",        "Implementar en Kotlin", TaskPriority.HIGH,   dueDate = "2026-05-10"),
        Tarea("t5", "c4", "Práctica de reacciones","Leer cap. 5",           TaskPriority.LOW,    dueDate = "2026-05-05"),
    )
    private var nextId = 6
    // CRUD completo en memoria
}
```

---

### 4. Crear `MockExamenRepository`

Archivo: `composeApp/src/webMain/kotlin/org/owlcode/edutrack/data/repository/MockExamenRepository.kt`

```kotlin
class MockExamenRepository : ExamenRepository {
    private val examenes = mutableListOf(
        Examen("e1", "c1", "Parcial 1 — Cálculo",      tema = "Límites",          fecha = "2026-05-10", horaInicio = "08:00"),
        Examen("e2", "c2", "Examen de Física",          tema = "Mov. Rectilíneo",  fecha = "2026-04-28", horaInicio = "10:30"),
        Examen("e3", "c3", "Examen Programación",       tema = "Algoritmos",       fecha = "2026-05-15", horaInicio = "10:00"),
        Examen("e4", "c4", "Parcial Química",           tema = "Enlace Químico",   fecha = "2026-05-12", horaInicio = "09:00"),
    )
    private var nextId = 5
    // CRUD completo en memoria
}
```

---

### 5. Crear `MockPersonalRepository`

Archivo: `composeApp/src/webMain/kotlin/org/owlcode/edutrack/data/repository/MockPersonalRepository.kt`

```kotlin
class MockPersonalRepository : PersonalRepository {
    private val eventos = mutableListOf(
        EventoPersonal("p1", "Reunión de Estudio",  "Biblioteca · Sala 3",     "2026-04-28", "16:00", "18:00"),
        EventoPersonal("p2", "Repaso Final",         "Sala de estudio virtual", "2026-05-01", "19:00", "21:00"),
        EventoPersonal("p3", "Asesoría con tutor",   "Cubículo 204",            "2026-05-06", "11:00", "12:00"),
    )
    private var nextId = 4
    // CRUD completo en memoria
}
```

---

### 6. Actualizar `DataModule.kt`

Archivo: `composeApp/src/webMain/kotlin/org/owlcode/edutrack/data/di/DataModule.kt`

Cambiar la sección de repositorios para elegir entre mock o real según `AppConfig.useMockData`:

```kotlin
val dataModule = module {
    includes(coreModule)

    // ── API Services — solo se instancian si NO es mock ────────────────────
    if (!AppConfig.useMockData) {
        single { AuthApiService(get(), AppConfig.API_BASE_URL) }
        single { CourseApiService(get(), AppConfig.API_BASE_URL) }
        single { ClaseApiService(get(), AppConfig.API_BASE_URL) }
        single { TareaApiService(get(), AppConfig.API_BASE_URL) }
        single { ExamenApiService(get(), AppConfig.API_BASE_URL) }
        single { EventoPersonalApiService(get(), AppConfig.API_BASE_URL) }

        // Datasources locales
        single { AuthLocalDataSource(get()) }
        single { CourseLocalDataSource(get()) }
        single { ClaseLocalDataSource(get()) }
        single { TareaLocalDataSource(get()) }
        single { ExamenLocalDataSource(get()) }
        single { PersonalLocalDataSource(get()) }
    }

    // ── Repositorios ──────────────────────────────────────────────────────
    single<AuthRepository> {
        if (AppConfig.useMockData) MockAuthRepository()
        else AuthRepositoryImpl(get(), get())
    }
    single<CourseRepository> {
        if (AppConfig.useMockData) MockCourseRepository()
        else CourseRepositoryImpl(get(), get(), get())
    }
    single<ClaseRepository> {
        if (AppConfig.useMockData) MockClaseRepository()
        else ClaseRepositoryImpl(get(), get(), get())
    }
    single<TareaRepository> {
        if (AppConfig.useMockData) MockTareaRepository()
        else TareaRepositoryImpl(get(), get(), get())
    }
    single<ExamenRepository> {
        if (AppConfig.useMockData) MockExamenRepository()
        else ExamenRepositoryImpl(get(), get(), get())
    }
    single<PersonalRepository> {
        if (AppConfig.useMockData) MockPersonalRepository()
        else PersonalRepositoryImpl(get(), get(), get())
    }

    // CalendarRepository siempre usa mock (no tiene endpoint en la API)
    single<CalendarRepository> { MockCalendarRepository() }
}
```

---

## Flujo resultante

```
API_BASE_URL = http://localhost:8080   →  useMockData = true  →  Repos mock (sin llamadas HTTP)
API_BASE_URL = https://mi-api.railway.app  →  useMockData = false →  Repos reales (Ktor + backend)
```

- No se necesita levantar ningún backend para desarrollar en local.
- Al subir a producción (Railway u otro), solo cambia la variable `API_BASE_URL` en el `.env` o en las variables de entorno del CI — ningún código cambia.
- Los repositorios mock mantienen estado únicamente en memoria; al recargar la página se reinician.

---

## Consideraciones abiertas

1. **Credenciales mock de login**: `MockAuthRepository` acepta `123@gmail.com / 123`. Ajustar si se desea otro usuario de prueba.
2. **Persistencia de mocks**: Si se necesita que los cambios sobrevivan a recargas durante el desarrollo, los repositorios mock podrían leer/escribir en `localStorage` usando `StorageDriver`.
3. **APP_ENV vs. URL de localhost**: El plan usa `API_BASE_URL.contains("localhost")` para máxima robustez. Si se prefiere control explícito, se puede cambiar a `APP_ENV == "development"` en el `build.gradle.kts`.
4. **Datos coherentes entre mocks**: Los IDs de cursos en los mocks de `Clase`, `Tarea` y `Examen` deben coincidir con los IDs de `MockCourseRepository` (`c1`–`c4`) para que los filtros por `courseId` funcionen correctamente.


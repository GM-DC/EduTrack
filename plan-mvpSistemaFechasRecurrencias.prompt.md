# Plan: MVP Agenda Académica — Sistema Completo de Tipos y Calendario

Construir sobre la arquitectura existente (CRUD de `Course`/`CalendarEvent`, vistas Mes/Semana/Día, `LocalStorageDriver`) para introducir las 4 entidades tipadas reales (`Clase`, `Tarea`, `Examen`, `Personal`), un motor de recurrencias puro y un modelo visual unificado `CalendarItem`, sin romper lo que ya funciona. Las tres microdecisiones quedan cerradas: **Tarea sin recurrencia en MVP**, **Examen sin `endTime` → auto +1h**, **Clase A-tu-ritmo sin hora → all-day**.

---

## Paso 1 — Modelos de dominio tipados

Crear en `domain/model/` los siguientes archivos nuevos:

- `Enums.kt`: `RecurrenceType` (NONE/DAILY/WEEKLY/MONTHLY), `RecurrenceEndType` (NEVER/UNTIL_DATE/AFTER_OCCURRENCES), `ClassMode` (PRESENTIAL/LIVE/SELF_PACED), `TaskStatus` (PENDING/IN_PROGRESS/SUBMITTED/OVERDUE), `ExamStatus` (PENDING/TAKEN/RESCHEDULED/CANCELLED).
- `RecurrenceRule.kt`: `data class @Serializable` con `type`, `interval`, `daysOfWeek: Set<Int>` (0=Lu…6=Do), `endType`, `untilDate: String?`, `occurrenceCount: Int?`.
- `Clase.kt`, `Tarea.kt`, `Examen.kt`, `EventoPersonal.kt`: `data class @Serializable` con los campos exactos del spec (punto 11):

```
Clase:       id, courseId, titulo?, modalidad, docente?, aula, enlace, notas,
             startDate, endDate, startTime?, endTime?,
             recurrenceRule?, daysOfWeek, isCancelled, parentSeriesId?

Tarea:       id, courseId, titulo, descripcion, prioridad, archivos?, checklist?,
             startDate?, dueDate, dueTime?, endDate?, estado

Examen:      id, courseId, titulo, tema, puntaje?, fecha,
             horaInicio?, horaFin?, duracion?, estado

EventoPersonal: id, titulo, descripcion?, fecha, horaInicio?, horaFin?
```

- Actualizar `Course.kt` agregando `locationOrPlatform: String = ""` y `credits: Int? = null`.
- El `CalendarEvent.kt` existente **se mantiene temporalmente** hasta que `CalendarItem` lo reemplace en el ViewModel.

---

## Paso 2 — `CalendarItem`: modelo visual unificado

Crear `domain/model/CalendarItem.kt`:

```kotlin
data class CalendarItem(
    val id: String,
    val sourceId: String,           // id de la entidad original
    val type: EventType,            // CLASS / TASK / EXAM / PERSONAL
    val title: String,
    val date: String,               // "YYYY-MM-DD"
    val startTime: String? = null,  // "HH:mm"
    val endTime: String? = null,    // "HH:mm"
    val isAllDay: Boolean = false,
    val courseColor: String? = null,
    val courseId: String? = null,
    val status: String? = null,
    val isDeadline: Boolean = false
)
```

Este es el **único** tipo que la UI del calendario consume.

---

## Paso 3 — Motor de recurrencias puro

Crear `domain/engine/RecurrenceEngine.kt` con funciones de extensión puras usando `kotlinx-datetime` (sin side-effects, sin coroutines):

- `Clase.toCalendarItems(range: ClosedRange<LocalDate>): List<CalendarItem>`
  - Sin recurrencia → evento único.
  - Con recurrencia: itera por `daysOfWeek` + `interval`, respeta `endType` / `untilDate` / `occurrenceCount`.
  - Si `startTime` es nulo → `isAllDay = true` (clase A tu ritmo).
  - Omite ocurrencias con `isCancelled = true`.

- `Tarea.toCalendarItem(): CalendarItem`
  - Usa `dueDate` / `dueTime`; si sin hora → `isAllDay = true`, `isDeadline = true`.

- `Examen.toCalendarItem(): CalendarItem`
  - Si sin `horaFin` → calcula `horaInicio + 1h` como `endTime`.

- `EventoPersonal.toCalendarItem(): CalendarItem`
  - Si sin hora → `isAllDay = true`.

---

## Paso 4 — Capa de datos: DTOs, datasources y repositorios

Por cada entidad nueva, siguiendo el patrón de `CalendarLocalDataSource`:

**DTOs** en `data/remote/dto/`:
- `ClaseDto`, `TareaDto`, `ExamenDto`, `PersonalDto` — `@Serializable`, campos idénticos a las entidades de dominio.

**Datasources** en `data/local/datasource/`:
- `ClaseLocalDataSource` (store `"clases"`)
- `TareaLocalDataSource` (store `"tareas"`)
- `ExamenLocalDataSource` (store `"examenes"`)
- `PersonalLocalDataSource` (store `"personales"`)

**Interfaces de repositorio** en `domain/repository/`:
```kotlin
interface ClaseRepository {
    suspend fun getClasesByCourse(courseId: String): AppResult<List<Clase>>
    suspend fun getClase(id: String): AppResult<Clase?>
    suspend fun addClase(clase: Clase): AppResult<Clase>
    suspend fun updateClase(clase: Clase): AppResult<Clase>
    suspend fun deleteClase(id: String): AppResult<Unit>
}
// Misma estructura para TareaRepository, ExamenRepository, PersonalRepository
```

**Implementaciones** en `data/repository/`:
- `ClaseRepositoryImpl`, `TareaRepositoryImpl`, `ExamenRepositoryImpl`, `PersonalRepositoryImpl`.

Actualizar `CourseFormDialog` para incluir los campos nuevos `locationOrPlatform` y `credits`.

---

## Paso 5 — Ruta `CourseDetail` + pantalla de gestión académica

- Añadir `CourseDetail` a `Route.kt`:
  ```kotlin
  data object CourseDetail : Route("courses/{courseId}") {
      fun withId(id: String) = "courses/$id"
  }
  ```
- Registrar el `composable` correspondiente en `App.kt` con extracción del argumento `courseId`.
- Hacer que cada `CourseCard` en `CourseScreen` navegue a `CourseDetail/{id}`.
- Crear `CourseDetailScreen` con:
  - `TopAppBar` que muestra el nombre del curso y botón atrás.
  - 3 tabs: **Clases / Tareas / Exámenes**.
  - Cada tab: lista de items + FAB de creación del tipo correspondiente.
  - Este es el **punto de entrada principal** para crear los 3 tipos académicos.

---

## Paso 6 — Actualizar `CalendarViewModel` y `AgendaUiState`

En `CalendarState.kt`:
```kotlin
// Cambiar:
val eventsByDate: Map<String, List<CalendarEvent>>
// Por:
val eventsByDate: Map<String, List<CalendarItem>>
// Agregar:
val activeFilter: CalendarFilter = CalendarFilter.ALL
```

Enum nuevo `CalendarFilter`:
```kotlin
enum class CalendarFilter { ALL, ACADEMIC, PERSONAL }
```

En `CalendarViewModel`:
- Inyectar `ClaseRepository`, `TareaRepository`, `ExamenRepository`, `PersonalRepository`.
- `loadEvents()` consulta los 4 repositorios, aplica el motor sobre el rango visible (mes actual ±2 meses), aplica el filtro activo y produce `Map<String, List<CalendarItem>>`.
- Añadir `setFilter(filter: CalendarFilter)`.

---

## Paso 7 — Actualizar vistas de calendario

**`MonthCalendar`** (componente):
- Recibir `Map<String, List<CalendarItem>>` en lugar de `Set<String>`.
- Dibujar hasta 3 puntos por día, coloreados con `courseColor`:
  - Clase → punto sólido.
  - Tarea → punto con borde o forma de check.
  - Examen → punto más prominente / icono alerta.
  - Personal → punto neutro (color del sistema).

**`WeekView`** y **vista Día**:
- Agregar **zona superior fija** (no forma parte del timeline, altura fija ~40dp) para items con `isAllDay = true` o `isDeadline = true`.
- Zona inferior = timeline de 07:00–22:00 para Clases, Exámenes y Personales con hora.
- `EventBlock` colorea el fondo con `courseColor`.

**`CalendarScreen`**:
- Agregar `FilterChipRow` con las 3 opciones (`Todo`, `Académico`, `Personal`) encima de las vistas.

---

## Paso 8 — Formularios tipados y `RecurrencePickerDialog`

Reemplazar `EventFormDialog` con 4 formularios específicos en `features/calendar/components/` (o `features/courses/components/`):

**`ClaseFormDialog`**:
- Dropdown de `ClassMode` (Presencial / En vivo / A tu ritmo).
- Campos: aula/enlace (según modalidad), notas.
- Fechas: `startDate` (obligatorio) y `endDate`.
- Horas: `startTime` y `endTime` (ambas opcionales si modalidad = A tu ritmo).
- Toggle "Repetir" → abre `RecurrencePickerDialog`.

**`RecurrencePickerDialog`**:
- Selector de tipo: No repetir / Diario / Semanal / Mensual.
- Si Semanal: chip-row **L M X J V S D** (toggle múltiple).
- Condición de fin: Nunca / Hasta fecha / Después de N veces.
- Resumen dinámico: `"Se repite cada lunes y miércoles hasta 10 jul"`.

**`TareaFormDialog`**:
- Título, descripción, prioridad (dropdown).
- `dueDate` obligatorio + `dueTime` opcional.
- Estado inicial (PENDING).

**`ExamenFormDialog`**:
- Título, tema, puntaje opcional.
- `fecha` obligatorio, `horaInicio` opcional.
- `horaFin` opcional — hint: `"Vacío = +1 hora automática"`.

**`PersonalFormDialog`**:
- Título, descripción opcional.
- `fecha` obligatorio, `horaInicio` / `horaFin` opcionales.

---

## Paso 9 — Edición de series y validaciones

**Edición de Clase recurrente**:
- Al editar una `Clase` con `parentSeriesId != null`, mostrar `EditSeriesDialog`:
  - "Solo esta ocurrencia" → crea nueva `Clase` con `isCancelled = false` y el `parentSeriesId` original; cancela la ocurrencia original.
  - "Toda la serie" → edita directamente la entidad padre.

**Validaciones inline** en cada formulario:
- `Clase`: `startDate ≤ endDate`, `startTime < endTime` (si ambas presentes), semanal → al menos 1 día seleccionado.
- `Tarea`: `dueDate` obligatorio, `startDate ≤ dueDate` si existe.
- `Examen`: si ambas horas → `horaInicio < horaFin`.
- `RecurrenceRule`: `interval ≥ 1`, `occurrenceCount ≥ 1` si aplica.

---

## Paso 10 — DI: registrar módulos nuevos

En `CalendarModule.kt` y módulo de datos:

```kotlin
// Datasources
single { ClaseLocalDataSource(get()) }
single { TareaLocalDataSource(get()) }
single { ExamenLocalDataSource(get()) }
single { PersonalLocalDataSource(get()) }

// Repositorios
single<ClaseRepository>   { ClaseRepositoryImpl(get()) }
single<TareaRepository>   { TareaRepositoryImpl(get()) }
single<ExamenRepository>  { ExamenRepositoryImpl(get()) }
single<PersonalRepository>{ PersonalRepositoryImpl(get()) }

// ViewModel actualizado con 4 repositorios
viewModel { CalendarViewModel(get(), get(), get(), get(), get(), get()) }
```

---

## Orden de ejecución recomendado

```
1  → Paso 1  Modelos de dominio + RecurrenceRule
2  → Paso 2  CalendarItem
3  → Paso 3  RecurrenceEngine (funciones puras → fácil de testear)
4  → Paso 4  DTOs + Datasources + Repositorios
5  → Paso 6  CalendarViewModel y estado actualizado
6  → Paso 5  Ruta CourseDetail + CourseDetailScreen
7  → Paso 7  Vistas de calendario actualizadas
8  → Paso 8  Formularios tipados + RecurrencePickerDialog
9  → Paso 9  Edición de series + Validaciones
10 → Paso 10 DI final
```

---

## Reglas del MVP (resumen rápido)

| Tipo          | Bloque horario | Recurrencia     | Depende de Curso |
|---------------|---------------|-----------------|------------------|
| Clase         | ✅ Sí          | ✅ Semanal       | ✅ Sí             |
| Tarea         | ❌ No (deadline)| ❌ No (MVP)     | ✅ Sí             |
| Examen        | ✅ Sí          | ❌ No            | ✅ Sí             |
| Personal      | ✅ Si tiene hora| ❌ No (MVP)     | ❌ No             |

---

## Consideraciones abiertas

1. **Migración de datos**: el store `"edu_calendar"` (basado en `CalendarEvent` genérico) quedará huérfano. ¿Borrarlo silenciosamente en el primer arranque tras el cambio o mostrar un aviso al usuario?
2. **Creación desde calendario**: ¿el FAB del `CalendarScreen` también abre un selector de tipo → curso antes del formulario, además del flujo principal desde `CourseDetail`?
3. **Tests del motor**: las funciones puras del Paso 3 son ideales para unit tests en `webTest/`. ¿Se incluyen en esta iteración?


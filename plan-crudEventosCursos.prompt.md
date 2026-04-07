# Plan: CRUD de Eventos y Cursos

El proyecto ya tiene la infraestructura base (repositorio, local datasource, API, ViewModel) para eventos, pero **le falta `updateEvent`** y toda la **UI de escritura** (crear/editar/borrar). Los cursos no existen en ninguna capa. El plan sigue la arquitectura Clean Architecture + Koin + StateFlow ya establecida.

---

## Pasos

### 1. Completar el contrato de eventos

Agregar `suspend fun updateEvent(event: CalendarEvent): AppResult<CalendarEvent>` e implementarlo en:

- `domain/repository/CalendarRepository.kt` — interfaz
- `data/repository/CalendarRepositoryImpl.kt` — llama a `remote.updateEvent(token, event.toDto())` + `local.saveEvent(dto)`
- `data/repository/MockCalendarRepository.kt` — reemplaza el elemento en la lista en memoria
- `data/remote/api/CalendarApiService.kt` — añade `suspend fun updateEvent(token, dto)` con `client.put("$baseUrl/events/${dto.id}")`

---

### 2. Crear la capa de dominio para Cursos

Archivos nuevos en `domain/`:

- `model/Course.kt` — `data class Course(id, name, color, teacher, description)`
- `repository/CourseRepository.kt` — interfaz con:
  - `getCourses(): AppResult<List<Course>>`
  - `getCourseById(id): AppResult<Course?>`
  - `addCourse(course): AppResult<Course>`
  - `updateCourse(course): AppResult<Course>`
  - `deleteCourse(id): AppResult<Unit>`

---

### 3. Implementar la capa de datos para Cursos

Siguiendo el mismo patrón de `CalendarRepositoryImpl`:

- `data/remote/dto/CourseDto.kt` — `@Serializable data class CourseDto(...)`
- `data/remote/api/CourseApiService.kt` — Ktor: GET `/courses`, POST, PUT `/{id}`, DELETE `/{id}`
- `data/local/datasource/CourseLocalDataSource.kt` — `StorageDriver` con store `"courses"`
- `data/remote/mapper/RemoteMappers.kt` — extensiones `CourseDto.toDomain()` y `Course.toDto()`
- `data/local/mapper/` — extensiones locales si aplica
- `data/repository/CourseRepositoryImpl.kt` — offline-first: lectura local, escritura remota + local
- `data/repository/MockCourseRepository.kt` — lista en memoria con datos de ejemplo

---

### 4. UI de CRUD para Eventos

Cambios en `features/calendar/`:

- **`CalendarViewModel.kt`** — añadir:
  - `fun showCreateEventForm()` / `fun showEditEventForm(event: CalendarEvent)` / `fun dismissEventForm()`
  - `fun saveEvent(event: CalendarEvent)` — llama a `addEvent` o `updateEvent` según si el id está vacío
  - `fun deleteEvent(id: String)` — con confirmación vía estado
- **`CalendarState.kt`** — añadir campos:
  - `val showEventForm: Boolean = false`
  - `val editingEvent: CalendarEvent? = null` — `null` = modo creación
  - `val pendingDeleteId: String? = null` — para el diálogo de confirmación
- **`components/EventFormDialog.kt`** — `AlertDialog` con campos: título, descripción, fecha, hora inicio, hora fin, tipo (`DropdownMenu` de `EventType`); validación básica (título no vacío, hora inicio < hora fin)
- **`components/DeleteConfirmDialog.kt`** — `AlertDialog` genérico de confirmación
- **`components/AgendaEventCard.kt`** — añadir `onEdit: () -> Unit` y `onDelete: () -> Unit`; mostrar botones de ícono (lápiz / basurero) en la tarjeta
- **`CalendarScreen.kt`** — añadir `FloatingActionButton` (ícono `+`) en el `Scaffold` para crear evento en el día seleccionado; mostrar `EventFormDialog` y `DeleteConfirmDialog` condicionalmente según estado

---

### 5. Feature completa de Cursos

Archivos nuevos en `features/courses/`:

- `CourseUiState.kt` — `data class CourseUiState(courses, isLoading, error, showForm, editingCourse)`
- `CourseViewModel.kt` — `loadCourses`, `showCreateForm`, `showEditForm`, `dismissForm`, `saveCourse`, `deleteCourse`
- `CourseScreen.kt` — `Scaffold` con `TopAppBar`, `LazyColumn` de cursos y `FloatingActionButton`
- `components/CourseCard.kt` — tarjeta con color de curso, nombre, profesor, botones editar/borrar
- `components/CourseFormDialog.kt` — formulario con campos nombre, profesor, descripción, selector de color
- `di/CourseModule.kt` — `viewModel { CourseViewModel(get()) }`

---

### 6. Navegación y DI

- **`features/app/Route.kt`** — agregar `data object Courses : Route("courses")`
- **`App.kt`** — registrar `composable(Route.Courses.path) { CourseScreen(...) }`; añadir `BottomNavigationBar` con ítems **Agenda** y **Cursos** para facilitar la navegación entre secciones
- **`data/di/DataModule.kt`** — registrar `single<CourseRepository> { MockCourseRepository() }`
- **`features/courses/di/CourseModule.kt`** — incluir en la lista de módulos de Koin en `main.kt`

---

## Consideraciones

1. **Relación Evento ↔ Curso**: ¿un `CalendarEvent` debería tener un `courseId: String?` opcional? Esto permitiría filtrar eventos por curso en `CourseScreen`. Opciones:
   - **A)** Añadir `courseId: String?` al modelo `CalendarEvent` ahora (afecta DTO, mapper, formulario).
   - **B)** Mantenerlos desacoplados por ahora y relacionarlos en una iteración futura.

2. **Validación del formulario**: manejar con un `FormState` local dentro del `Dialog` (sin ViewModel propio). Errores inline bajo cada campo con `Text(color = error)`.

3. **Confirmación de borrado**: usar `DeleteConfirmDialog` reutilizable tanto para eventos como para cursos antes de ejecutar la operación.

4. **IDs en modo mock**: generar con `uuid()` o un simple contador incremental para evitar colisiones en `MockCourseRepository` y `MockCalendarRepository`.

5. **Color de curso**: representar como `String` hexadecimal (`"#4A90D9"`) en el modelo para ser serializable; convertir a `Color` en la capa de UI con una extensión `String.toComposeColor()`.


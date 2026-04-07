# Plan: Calendario Agenda — EduTrack

Implementar una pantalla de agenda escolar completa en `features/calendar/` con calendario mensual interactivo, lista de eventos por día y diseño visual pulido. Se construye en 7 fases incrementales.

---

## Cambios de infraestructura

### `gradle/libs.versions.toml`
Agregar en `[versions]`:
```toml
kotlinx-datetime = "0.7.1"
```
Agregar en `[libraries]`:
```toml
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }
```

### `build.gradle.kts`
Agregar en `commonMain.dependencies`:
```kotlin
implementation(libs.kotlinx.datetime)
```

---

## Modelo de dominio actualizado

### `domain/model/CalendarEvent.kt`
```kotlin
data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String = "",
    val startTime: String = "",   // "HH:mm"
    val endTime: String = "",     // "HH:mm"
    val type: EventType,
    val date: String              // "YYYY-MM-DD"
)

enum class EventType { CLASS, EXAM, TASK, PERSONAL }
```

### `data/remote/dto/CalendarEventDto.kt`
```kotlin
@Serializable
data class CalendarEventDto(
    val id: String,
    val title: String,
    val description: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val type: String = "CLASS",
    val date: String
)
```

### `data/remote/mapper/RemoteMappers.kt`
```kotlin
fun UserDto.toDomain(): User = User(
    id    = id, name  = name, email = email,
    role  = runCatching { UserRole.valueOf(role.uppercase()) }.getOrDefault(UserRole.STUDENT)
)

fun CalendarEventDto.toDomain(): CalendarEvent = CalendarEvent(
    id = id, title = title, description = description,
    startTime = startTime, endTime = endTime,
    type = runCatching { EventType.valueOf(type.uppercase()) }.getOrDefault(EventType.CLASS),
    date = date
)

fun CalendarEvent.toDto(): CalendarEventDto = CalendarEventDto(
    id = id, title = title, description = description,
    startTime = startTime, endTime = endTime,
    type = type.name, date = date
)
```

---

## Mock data — 13 eventos escolares

### `data/repository/MockCalendarRepository.kt`
```kotlin
private val events = mutableListOf(
    // Hoy: 2026-04-05
    CalendarEvent("1",  "Matemáticas I",          "Sala B-302 · Prof. Martínez", "08:00", "10:00", EventType.CLASS,    "2026-04-05"),
    CalendarEvent("2",  "Examen de Física",        "Aula Magna · Prof. Ruiz",     "10:30", "12:30", EventType.EXAM,     "2026-04-05"),
    CalendarEvent("3",  "Programación Orientada",  "Lab de Cómputo 201",          "14:00", "16:00", EventType.CLASS,    "2026-04-05"),
    CalendarEvent("4",  "Cálculo II",              "Sala A-101 · Prof. Sánchez",  "08:00", "10:00", EventType.CLASS,    "2026-04-07"),
    CalendarEvent("5",  "Tarea: Integrales",       "Entregar por plataforma",     "23:59", "23:59", EventType.TASK,     "2026-04-07"),
    CalendarEvent("6",  "Entrega Proyecto Final",  "Campus Virtual",              "23:59", "23:59", EventType.TASK,     "2026-04-10"),
    CalendarEvent("7",  "Reunión de Estudio",      "Biblioteca · Sala 3",         "16:00", "18:00", EventType.PERSONAL, "2026-04-10"),
    CalendarEvent("8",  "Física Experimental",     "Lab de Física · Piso 1",      "08:00", "10:00", EventType.CLASS,    "2026-04-13"),
    CalendarEvent("9",  "Examen de Programación",  "Lab de Cómputo 305",          "10:00", "12:00", EventType.EXAM,     "2026-04-15"),
    CalendarEvent("10", "Tarea: Algoritmos",       "Entregar por plataforma",     "23:59", "23:59", EventType.TASK,     "2026-04-18"),
    CalendarEvent("11", "Química General",         "Lab de Química · Piso 2",     "08:00", "10:00", EventType.CLASS,    "2026-04-21"),
    CalendarEvent("12", "Repaso Final",            "Sala de estudio virtual",     "19:00", "21:00", EventType.PERSONAL, "2026-04-25"),
    CalendarEvent("13", "Examen Final de Cálculo", "Aula Magna",                  "09:00", "12:00", EventType.EXAM,     "2026-04-28"),
)
```

---

## Estado y ViewModel

### `features/calendar/CalendarState.kt`
```kotlin
data class AgendaUiState(
    val currentYear: Int,
    val currentMonth: Int,        // 1-12
    val selectedDateStr: String,  // "YYYY-MM-DD"
    val eventsByDate: Map<String, List<CalendarEvent>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val eventsForSelectedDay: List<CalendarEvent>
        get() = eventsByDate[selectedDateStr].orEmpty().sortedBy { it.startTime }

    val datesWithEvents: Set<String>
        get() = eventsByDate.keys
}
```

### `features/calendar/CalendarViewModel.kt`
```kotlin
class CalendarViewModel(
    private val calendarRepository: CalendarRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(buildInitialState())
    val state: StateFlow<AgendaUiState> = _state.asStateFlow()

    init { loadEvents() }

    private fun buildInitialState(): AgendaUiState {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return AgendaUiState(
            currentYear     = today.year,
            currentMonth    = today.monthNumber,
            selectedDateStr = today.toString()
        )
    }

    fun loadEvents() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = calendarRepository.getEvents()) {
                is AppResult.Success -> _state.update {
                    it.copy(eventsByDate = result.data.groupBy { e -> e.date }, isLoading = false, error = null)
                }
                is AppResult.Error -> _state.update {
                    it.copy(isLoading = false, error = result.error.message)
                }
            }
        }
    }

    fun selectDate(dateStr: String) = _state.update { it.copy(selectedDateStr = dateStr) }

    fun nextMonth() = _state.update {
        val next = LocalDate(it.currentYear, it.currentMonth, 1).plus(1, DateTimeUnit.MONTH)
        it.copy(currentYear = next.year, currentMonth = next.monthNumber)
    }

    fun prevMonth() = _state.update {
        val prev = LocalDate(it.currentYear, it.currentMonth, 1).minus(1, DateTimeUnit.MONTH)
        it.copy(currentYear = prev.year, currentMonth = prev.monthNumber)
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch { authRepository.logout(); onLoggedOut() }
    }
}
```

---

## Componentes UI

### `features/calendar/components/MonthCalendar.kt`

Grilla 7 columnas (Lun–Dom) con:
- Navegación `‹ Abril 2026 ›`
- Cabecera de días de semana
- `DayCell` por cada día: círculo de selección, resaltado de hoy, punto indicador de eventos

```kotlin
@Composable
fun MonthCalendar(
    year: Int, month: Int,
    selectedDateStr: String,
    eventDates: Set<String>,
    onDaySelected: (String) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
)

@Composable
private fun DayCell(
    day: Int?, isToday: Boolean, isSelected: Boolean,
    hasEvents: Boolean, onClick: () -> Unit, modifier: Modifier
)
```

**Matemáticas del calendario:**
```kotlin
val startOffset = LocalDate(year, month, 1).dayOfWeek.isoDayNumber - 1  // 0=Lun
val daysInMonth = LocalDate(year, if (month==12) 1 else month+1, 1)
                      .minus(1, DateTimeUnit.DAY).dayOfMonth
val dateStr = "$year-${month.padStart2}-${day.padStart2}"
```

### `features/calendar/components/AgendaEventCard.kt`

Tarjeta con tres zonas:
```
[Hora inicio] | [Barra color 4dp] | [Título + descripción] [Badge tipo]
[Hora fin   ]
```

Colores por tipo:
| EventType | Color |
|-----------|-------|
| CLASS     | `#4A90D9` azul |
| EXAM      | `#E05252` rojo |
| TASK      | `#F5A623` ámbar |
| PERSONAL  | `#7B68EE` violeta |

---

## Pantalla principal

### `features/calendar/CalendarScreen.kt`

```
Scaffold
├── TopAppBar ("Agenda" + botón "Salir")
└── Column (fillMaxSize, padding 16dp)
    ├── Spacer(8dp)
    ├── MonthCalendar          ← card redondeada, elevation 2dp
    ├── Spacer(16dp)
    ├── Text(selectedLabel)    ← "Hoy, 5 de abril" / "Lunes, 7 de abril"
    ├── Spacer(8dp)
    └── Box(weight(1f))
        ├── Loading → CircularProgressIndicator
        ├── Error   → Text + Button("Reintentar")
        ├── Vacío   → Text("Sin eventos para este día")
        └── Eventos → LazyColumn { AgendaEventCard(...) }
```

**Formato de fecha seleccionada:**
```kotlin
when (date) {
    today    -> "Hoy, ${d.dayOfMonth} de ${MONTH_ES[d.monthNumber]}"
    tomorrow -> "Mañana, ${d.dayOfMonth} de ${MONTH_ES[d.monthNumber]}"
    else     -> "${DAY_ES[d.dayOfWeek.isoDayNumber].capitalize()}, ${d.dayOfMonth} de ..."
}
```

---

## Estructura de archivos resultante

```
features/calendar/
├── CalendarState.kt          → AgendaUiState (data class)
├── CalendarViewModel.kt      → selectDate / nextMonth / prevMonth / loadEvents
├── CalendarScreen.kt         → AgendaScreen composable principal
├── components/
│   ├── MonthCalendar.kt      → Card con grilla 7×N + DayCell
│   └── AgendaEventCard.kt    → Tarjeta horario + barra color + badge
└── di/
    └── CalendarModule.kt     → sin cambios
```

---

## Fases pendientes

| Fase | Descripción | Prioridad |
|------|-------------|-----------|
| FASE 5 | Persistencia local (IndexedDB ya implementado) | Media |
| FASE 6 | Cursos recurrentes, recordatorios, filtro por tipo | Alta |
| FASE 7 | Sincronización, vista semanal, temas claro/oscuro | Futura |

### Detalles de FASE 6 (próxima iteración)
- `CalendarEvent` + campo `recurrence: Recurrence?` (NONE, DAILY, WEEKLY, MONTHLY)
- Filtro por `EventType` en la cabecera de la lista
- `addEvent` UI con formulario (título, horario, tipo, aula, profesor)
- Indicador visual de conflictos de horario en el mismo día


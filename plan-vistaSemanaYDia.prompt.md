# Plan: Vistas Semana y Día con estado compartido

Refactorizar el estado y ViewModel existentes para soportar tres modos (Mes/Semana/Día), luego construir las vistas semana y día reutilizando componentes compartidos (`EventBlock`, `TimeColumn`). Todo en un único `CalendarScreen` con tabs.

---

## Contexto actual

- `AgendaUiState` con `currentYear`, `currentMonth`, `selectedDateStr`, `eventsByDate: Map<String, List<CalendarEvent>>`
- `CalendarViewModel` con `loadEvents()`, `selectDate()`, `nextMonth()`, `prevMonth()`, `logout()`
- `CalendarEvent` con `id`, `title`, `description`, `startTime` ("HH:mm"), `endTime` ("HH:mm"), `type`, `date` ("YYYY-MM-DD")
- Componentes: `AgendaEventCard`, `MonthCalendar`, `DayCell`
- `CalendarScreen` único con `LazyColumn` mostrando calendario mes + lista de eventos

---

## Estructura objetivo

```
features/calendar/
├── CalendarState.kt              ← refactor: añadir viewMode + visibleWeekStart
├── CalendarViewModel.kt          ← refactor: nextPeriod/prevPeriod + changeMode
├── CalendarScreen.kt             ← entrada única con tabs + when(viewMode)
├── utils/
│   └── CalendarDateUtils.kt      ← NUEVO: lógica de fechas pura
├── components/
│   ├── AgendaEventCard.kt        ← sin cambios
│   ├── MonthCalendar.kt          ← sin cambios
│   ├── CalendarModeTabs.kt       ← NUEVO: tab row MONTH/WEEK/DAY
│   ├── TimeColumn.kt             ← NUEVO: columna de horas 07–22
│   └── EventBlock.kt             ← NUEVO: bloque posicionado por hora
├── month/
│   └── MonthView.kt              ← NUEVO: extraído de CalendarScreen
├── week/
│   └── WeekView.kt               ← NUEVO
└── day/
    └── DayView.kt                ← NUEVO
```

---

## Pasos

### Paso 1 — Crear `CalendarViewMode.kt` y refactorizar `CalendarState.kt`

Añadir `enum class CalendarViewMode { MONTH, WEEK, DAY }` y extender `AgendaUiState` con:
- `viewMode: CalendarViewMode = CalendarViewMode.MONTH`
- `visibleWeekStart: String` ("YYYY-MM-DD", lunes de la semana visible)

Mantener `currentYear`, `currentMonth` y `eventsByDate` intactos para no romper la vista mes.

```kotlin
enum class CalendarViewMode { MONTH, WEEK, DAY }

data class AgendaUiState(
    val currentYear: Int,
    val currentMonth: Int,           // 1-12
    val selectedDateStr: String,     // "YYYY-MM-DD"
    val visibleWeekStart: String,    // "YYYY-MM-DD", siempre lunes
    val viewMode: CalendarViewMode = CalendarViewMode.MONTH,
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

---

### Paso 2 — Crear `utils/CalendarDateUtils.kt`

Funciones puras sin dependencias de UI:

```kotlin
// Lunes de la semana que contiene `date`
fun getWeekStart(date: LocalDate): LocalDate =
    date.minus(date.dayOfWeek.ordinal, DateTimeUnit.DAY)

// Lista de 7 fechas: lunes → domingo
fun getWeekDays(weekStart: LocalDate): List<LocalDate> =
    (0..6).map { weekStart.plus(it, DateTimeUnit.DAY) }

// Todos los días del mes
fun getMonthDays(year: Int, month: Int): List<LocalDate> {
    val first = LocalDate(year, month, 1)
    val total = daysInMonth(year, month)
    return (0 until total).map { first.plus(it, DateTimeUnit.DAY) }
}

// "HH:mm" → Pair(hora, minuto); "" → Pair(0, 0)
fun parseHourMinute(time: String): Pair<Int, Int> {
    if (time.isBlank()) return 0 to 0
    val parts = time.split(":")
    return (parts.getOrNull(0)?.toIntOrNull() ?: 0) to
           (parts.getOrNull(1)?.toIntOrNull() ?: 0)
}

// Minutos totales desde medianoche para "HH:mm"
fun toMinutes(time: String): Int {
    val (h, m) = parseHourMinute(time)
    return h * 60 + m
}
```

---

### Paso 3 — Refactorizar `CalendarViewModel.kt`

- Reemplazar `nextMonth()`/`prevMonth()` por `nextPeriod()`/`prevPeriod()` que deleguen según `viewMode`:
  - `MONTH` → avanza/retrocede un mes
  - `WEEK`  → avanza/retrocede 7 días en `visibleWeekStart`
  - `DAY`   → avanza/retrocede 1 día en `selectedDateStr`
- Añadir `changeMode(CalendarViewMode)`: al cambiar a `WEEK` actualiza `visibleWeekStart` a `getWeekStart(selectedDate)`.
- `selectDate(dateStr)`: actualiza `selectedDateStr` y si el modo es `WEEK` o `DAY` también sincroniza `visibleWeekStart`.

```kotlin
fun changeMode(mode: CalendarViewMode) = _state.update { s ->
    val date = LocalDate.parse(s.selectedDateStr)
    s.copy(
        viewMode         = mode,
        visibleWeekStart = getWeekStart(date).toString()
    )
}

fun nextPeriod() = _state.update { s ->
    when (s.viewMode) {
        CalendarViewMode.MONTH -> { /* avanza mes */ }
        CalendarViewMode.WEEK  -> { /* avanza 7 días en visibleWeekStart */ }
        CalendarViewMode.DAY   -> { /* avanza 1 día en selectedDateStr */ }
    }
}

fun prevPeriod() = _state.update { s ->
    when (s.viewMode) {
        CalendarViewMode.MONTH -> { /* retrocede mes */ }
        CalendarViewMode.WEEK  -> { /* retrocede 7 días en visibleWeekStart */ }
        CalendarViewMode.DAY   -> { /* retrocede 1 día en selectedDateStr */ }
    }
}
```

---

### Paso 4 — Crear componentes compartidos

#### `components/CalendarModeTabs.kt`
Tab row fijo bajo el `TopAppBar` con tres pestañas: Mes / Semana / Día.

```kotlin
@Composable
fun CalendarModeTabs(
    selectedMode: CalendarViewMode,
    onModeSelected: (CalendarViewMode) -> Unit
)
```

#### `components/TimeColumn.kt`
Columna de etiquetas de hora (07:00 → 22:00). Constante compartida `HOUR_HEIGHT_DP = 64.dp`.

```kotlin
@Composable
fun TimeColumn(
    startHour: Int = 7,
    endHour: Int   = 22,
    modifier: Modifier = Modifier
)
```

#### `components/EventBlock.kt`
`Box` posicionado absolutamente dentro de la columna de tiempo.
- `topDp  = (toMinutes(event.startTime) - startHour * 60) / 60f * HOUR_HEIGHT_DP`
- `height = max(30.dp, (toMinutes(event.endTime) - toMinutes(event.startTime)) / 60f * HOUR_HEIGHT_DP)`
- Fallback si `startTime` vacío: bloque de 30 min solo con título.
- Reutiliza `EventType.color()` ya definido en `AgendaEventCard`.

```kotlin
@Composable
fun EventBlock(
    event: CalendarEvent,
    startHour: Int   = 7,
    modifier: Modifier = Modifier
)
```

---

### Paso 5 — Crear `week/WeekView.kt`

Estructura visual:
```
WeekView
├── WeekHeader        ← rango "31 mar – 6 abr 2026" + flechas prev/next
├── WeekDaysRow       ← Lu 31 | Ma 1 | Mi 2 ... Do 5 (toca → selectDate + DAY)
└── Box (scroll vertical)
    ├── TimeColumn
    └── Row (7 columnas)
        └── Box por cada día (altura = totalHours * HOUR_HEIGHT_DP)
            └── EventBlock por cada evento del día
```

Regla visual:
- `HOUR_HEIGHT_DP = 64.dp`
- `1 hora = 64.dp`, `30 min = 32.dp`
- Ancho por columna = `(screenWidth - timeColumnWidth) / 7`
- Hora de inicio visible: 07:00 | Hora de fin: 22:00 → altura total = `15 * 64.dp = 960.dp`

Implementación en 5 sub-pasos:
1. Header + fila de días + columnas vacías + horas
2. Eventos hardcodeados como bloques simples
3. Posicionamiento exacto por hora usando `EventBlock`
4. Scroll vertical con `LaunchedEffect` a hora actual
5. Tap en día → `selectDate()` + `changeMode(DAY)`

---

### Paso 6 — Crear `day/DayView.kt`

Igual que `WeekView` pero con una sola columna:
```
DayView
├── DayHeader         ← fecha completa + flechas prev/next
└── Box (scroll vertical)
    ├── TimeColumn
    └── Box (una columna, ancho completo)
        └── EventBlock por cada evento del día
```

Reutiliza exactamente `EventBlock` y `TimeColumn` de la vista semana.
`LaunchedEffect` al cargar para scroll automático a la hora actual.

---

### Paso 7 — Actualizar `CalendarScreen.kt`

1. Extraer vista mes actual a `month/MonthView.kt` (el `LazyColumn` con el calendario + lista de eventos).
2. Reemplazar el `LazyColumn` raíz por un `Column`:
   - `TopAppBar` con botón "Salir"
   - `CalendarModeTabs` fijo
   - `when(state.viewMode)` → `MonthView` / `WeekView` / `DayView`

```kotlin
@Composable
fun CalendarScreen(onLogout: () -> Unit, viewModel: CalendarViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Agenda") }, actions = { /* Salir */ }) }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            CalendarModeTabs(
                selectedMode   = state.viewMode,
                onModeSelected = viewModel::changeMode
            )
            when (state.viewMode) {
                CalendarViewMode.MONTH -> MonthView(state, viewModel)
                CalendarViewMode.WEEK  -> WeekView(state, viewModel)
                CalendarViewMode.DAY   -> DayView(state, viewModel)
            }
        }
    }
}
```

---

## Consideraciones técnicas

| Tema | Decisión |
|---|---|
| Posicionamiento eventos | `Box` con `Modifier.offset(y = topDp).height(heightDp)` — sin Canvas, compatible con KMP Web |
| `startTime`/`endTime` vacíos | `EventBlock` renderiza bloque de 30 min con solo título como fallback |
| `YearMonth` ausente en kotlinx-datetime | Se trabaja con `LocalDate` directamente en todas las utilidades |
| `LocalDate.now()` no existe | `Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date` |
| Semana empieza en | Lunes (`dayOfWeek.ordinal == 0`) |
| Retrocompatibilidad | `nextMonth()`/`prevMonth()` se mantienen como alias hasta que `MonthView` use `nextPeriod()`/`prevPeriod()` |

---

## Orden de implementación recomendado

| Bloque | Archivos | Prioridad |
|---|---|---|
| 1 | `CalendarViewMode.kt`, `CalendarState.kt`, `CalendarDateUtils.kt` | Alta |
| 2 | `CalendarViewModel.kt` refactor | Alta |
| 3 | `TimeColumn.kt`, `EventBlock.kt`, `CalendarModeTabs.kt` | Alta |
| 4 | `WeekView.kt` (estructura vacía + horas) | Alta |
| 5 | `WeekView.kt` (eventos posicionados) | Alta |
| 6 | `DayView.kt` | Alta |
| 7 | `MonthView.kt` + `CalendarScreen.kt` unificado | Media |
| 8 | Scroll a hora actual, línea de hora actual | Media |
| 9 | Animaciones, eventos superpuestos, drag & drop | Baja |


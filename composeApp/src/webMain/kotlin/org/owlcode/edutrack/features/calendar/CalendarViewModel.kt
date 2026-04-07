package org.owlcode.edutrack.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import org.owlcode.edutrack.core.result.AppResult
import org.owlcode.edutrack.domain.engine.toCalendarItem
import org.owlcode.edutrack.domain.engine.toCalendarItems
import org.owlcode.edutrack.domain.model.CalendarItem
import org.owlcode.edutrack.domain.model.EventType
import org.owlcode.edutrack.domain.model.EventoPersonal
import org.owlcode.edutrack.domain.repository.AuthRepository
import org.owlcode.edutrack.domain.repository.ClaseRepository
import org.owlcode.edutrack.domain.repository.CourseRepository
import org.owlcode.edutrack.domain.repository.ExamenRepository
import org.owlcode.edutrack.domain.repository.PersonalRepository
import org.owlcode.edutrack.domain.repository.TareaRepository
import org.owlcode.edutrack.features.calendar.utils.getWeekStart

class CalendarViewModel(
    private val claseRepository: ClaseRepository,
    private val tareaRepository: TareaRepository,
    private val examenRepository: ExamenRepository,
    private val personalRepository: PersonalRepository,
    private val courseRepository: CourseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(buildInitialState())
    val state: StateFlow<AgendaUiState> = _state.asStateFlow()

    init { loadEvents() }

    private fun buildInitialState(): AgendaUiState {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return AgendaUiState(
            currentYear      = today.year,
            currentMonth     = today.month.ordinal + 1,
            selectedDateStr  = today.toString(),
            visibleWeekStart = getWeekStart(today).toString()
        )
    }

    // ── Carga ──────────────────────────────────────────────────────────────────

    fun loadEvents() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Mapa courseId → color hex
            val coursesMap = when (val r = courseRepository.getCourses()) {
                is AppResult.Success -> r.data.associate { it.id to it.color }
                is AppResult.Error   -> emptyMap()
            }

            // Rango de generación: mes actual ±2 meses
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val rangeStart = LocalDate(today.year, today.month, 1).minus(2, DateTimeUnit.MONTH)
            val rangeEnd   = LocalDate(today.year, today.month, 1).plus(3, DateTimeUnit.MONTH)
                .minus(1, DateTimeUnit.DAY)
            val range = rangeStart..rangeEnd

            val items = mutableListOf<CalendarItem>()

            when (val r = claseRepository.getAllClases()) {
                is AppResult.Success -> r.data.forEach { clase ->
                    items.addAll(clase.toCalendarItems(range, coursesMap[clase.courseId]))
                }
                is AppResult.Error -> {}
            }

            when (val r = tareaRepository.getAllTareas()) {
                is AppResult.Success -> r.data.forEach { tarea ->
                    val item = tarea.toCalendarItem(coursesMap[tarea.courseId])
                    if (runCatching { LocalDate.parse(item.date) in range }.getOrDefault(false))
                        items.add(item)
                }
                is AppResult.Error -> {}
            }

            when (val r = examenRepository.getAllExamenes()) {
                is AppResult.Success -> r.data.forEach { examen ->
                    val item = examen.toCalendarItem(coursesMap[examen.courseId])
                    if (runCatching { LocalDate.parse(item.date) in range }.getOrDefault(false))
                        items.add(item)
                }
                is AppResult.Error -> {}
            }

            when (val r = personalRepository.getAllPersonales()) {
                is AppResult.Success -> r.data.forEach { personal ->
                    val item = personal.toCalendarItem()
                    if (runCatching { LocalDate.parse(item.date) in range }.getOrDefault(false))
                        items.add(item)
                }
                is AppResult.Error -> {}
            }

            val filtered = applyFilter(items, _state.value.activeFilter)
            _state.update {
                it.copy(
                    eventsByDate = filtered.groupBy { item -> item.date },
                    isLoading    = false,
                    error        = null
                )
            }
        }
    }

    private fun applyFilter(items: List<CalendarItem>, filter: CalendarFilter): List<CalendarItem> =
        when (filter) {
            CalendarFilter.ALL      -> items
            CalendarFilter.ACADEMIC -> items.filter { it.type != EventType.PERSONAL }
            CalendarFilter.PERSONAL -> items.filter { it.type == EventType.PERSONAL }
        }

    fun setFilter(filter: CalendarFilter) {
        _state.update { it.copy(activeFilter = filter) }
        loadEvents()
    }

    // ── Navegación de fecha/modo ───────────────────────────────────────────────

    fun selectDate(dateStr: String) = _state.update { s ->
        val date = LocalDate.parse(dateStr)
        s.copy(selectedDateStr = dateStr, visibleWeekStart = getWeekStart(date).toString())
    }

    fun changeMode(mode: CalendarViewMode) = _state.update { s ->
        val date = LocalDate.parse(s.selectedDateStr)
        s.copy(viewMode = mode, visibleWeekStart = getWeekStart(date).toString())
    }

    fun nextPeriod() = _state.update { s ->
        when (s.viewMode) {
            CalendarViewMode.MONTH -> {
                val next = LocalDate(s.currentYear, s.currentMonth, 1).plus(1, DateTimeUnit.MONTH)
                s.copy(currentYear = next.year, currentMonth = next.month.ordinal + 1)
            }
            CalendarViewMode.WEEK -> {
                val next = LocalDate.parse(s.visibleWeekStart).plus(7, DateTimeUnit.DAY)
                s.copy(visibleWeekStart = next.toString())
            }
            CalendarViewMode.DAY -> {
                val next = LocalDate.parse(s.selectedDateStr).plus(1, DateTimeUnit.DAY)
                s.copy(selectedDateStr = next.toString(), visibleWeekStart = getWeekStart(next).toString())
            }
        }
    }

    fun prevPeriod() = _state.update { s ->
        when (s.viewMode) {
            CalendarViewMode.MONTH -> {
                val prev = LocalDate(s.currentYear, s.currentMonth, 1).minus(1, DateTimeUnit.MONTH)
                s.copy(currentYear = prev.year, currentMonth = prev.month.ordinal + 1)
            }
            CalendarViewMode.WEEK -> {
                val prev = LocalDate.parse(s.visibleWeekStart).minus(7, DateTimeUnit.DAY)
                s.copy(visibleWeekStart = prev.toString())
            }
            CalendarViewMode.DAY -> {
                val prev = LocalDate.parse(s.selectedDateStr).minus(1, DateTimeUnit.DAY)
                s.copy(selectedDateStr = prev.toString(), visibleWeekStart = getWeekStart(prev).toString())
            }
        }
    }

    fun nextMonth() = _state.update { s ->
        val next = LocalDate(s.currentYear, s.currentMonth, 1).plus(1, DateTimeUnit.MONTH)
        s.copy(currentYear = next.year, currentMonth = next.month.ordinal + 1)
    }

    fun prevMonth() = _state.update { s ->
        val prev = LocalDate(s.currentYear, s.currentMonth, 1).minus(1, DateTimeUnit.MONTH)
        s.copy(currentYear = prev.year, currentMonth = prev.month.ordinal + 1)
    }

    // ── CRUD EventoPersonal ────────────────────────────────────────────────────

    fun showCreatePersonalForm() =
        _state.update { it.copy(showPersonalForm = true, editingPersonal = null) }

    fun showEditPersonalForm(personal: EventoPersonal) =
        _state.update { it.copy(showPersonalForm = true, editingPersonal = personal) }

    fun dismissPersonalForm() =
        _state.update { it.copy(showPersonalForm = false, editingPersonal = null) }

    fun savePersonal(personal: EventoPersonal) {
        viewModelScope.launch {
            val isNew  = personal.id.isBlank()
            val toSave = if (isNew) personal.copy(id = Clock.System.now().toEpochMilliseconds().toString())
                         else personal
            val result = if (isNew) personalRepository.addPersonal(toSave)
                         else       personalRepository.updatePersonal(toSave)
            when (result) {
                is AppResult.Success -> { loadEvents(); dismissPersonalForm() }
                is AppResult.Error   -> _state.update { it.copy(error = result.error.message) }
            }
        }
    }

    fun requestDelete(item: CalendarItem) =
        _state.update { it.copy(pendingDeleteItem = item) }

    fun cancelDelete() =
        _state.update { it.copy(pendingDeleteItem = null) }

    fun confirmDelete() {
        val item = _state.value.pendingDeleteItem ?: return
        viewModelScope.launch {
            when (item.type) {
                EventType.CLASS    -> claseRepository.deleteClase(item.sourceId)
                EventType.TASK     -> tareaRepository.deleteTarea(item.sourceId)
                EventType.EXAM     -> examenRepository.deleteExamen(item.sourceId)
                EventType.PERSONAL -> personalRepository.deletePersonal(item.sourceId)
            }
            loadEvents()
            _state.update { it.copy(pendingDeleteItem = null) }
        }
    }

    // ── Auth ───────────────────────────────────────────────────────────────────

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch { authRepository.logout(); onLoggedOut() }
    }
}

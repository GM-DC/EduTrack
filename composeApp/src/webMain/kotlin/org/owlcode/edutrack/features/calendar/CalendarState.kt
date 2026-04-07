package org.owlcode.edutrack.features.calendar

import org.owlcode.edutrack.domain.model.CalendarItem
import org.owlcode.edutrack.domain.model.EventoPersonal

enum class CalendarFilter { ALL, ACADEMIC, PERSONAL }

data class AgendaUiState(
    val currentYear: Int,
    val currentMonth: Int,           // 1-12
    val selectedDateStr: String,     // "YYYY-MM-DD"
    val visibleWeekStart: String,    // "YYYY-MM-DD", siempre lunes
    val viewMode: CalendarViewMode = CalendarViewMode.MONTH,
    val activeFilter: CalendarFilter = CalendarFilter.ALL,
    val eventsByDate: Map<String, List<CalendarItem>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // ── Personal CRUD ─────────────────────────────────────────────────────────
    val showPersonalForm: Boolean = false,
    val editingPersonal: EventoPersonal? = null,
    val pendingDeleteItem: CalendarItem? = null
) {
    val itemsForSelectedDay: List<CalendarItem>
        get() = eventsByDate[selectedDateStr].orEmpty()
            .sortedWith(compareByDescending<CalendarItem> { it.isAllDay }.thenBy { it.startTime ?: "23:59" })

    val datesWithEvents: Set<String>
        get() = eventsByDate.keys

    val itemsByDateForDots: Map<String, List<CalendarItem>>
        get() = eventsByDate
}

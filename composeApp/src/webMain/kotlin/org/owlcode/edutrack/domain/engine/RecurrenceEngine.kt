package org.owlcode.edutrack.domain.engine

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.owlcode.edutrack.domain.model.Clase
import org.owlcode.edutrack.domain.model.CalendarItem
import org.owlcode.edutrack.domain.model.EventType
import org.owlcode.edutrack.domain.model.EventoPersonal
import org.owlcode.edutrack.domain.model.Examen
import org.owlcode.edutrack.domain.model.RecurrenceEndType
import org.owlcode.edutrack.domain.model.RecurrenceType
import org.owlcode.edutrack.domain.model.Tarea

// ── Clase ────────────────────────────────────────────────────────────────────

/**
 * Genera la lista de [CalendarItem] para una [Clase] dentro del [range] dado.
 * - Sin recurrencia → un único evento.
 * - Con recurrencia WEEKLY → itera semana a semana respetando [Clase.daysOfWeek] e interval.
 * - [Clase.startTime] == null → [CalendarItem.isAllDay] = true (modalidad "A tu ritmo").
 */
fun Clase.toCalendarItems(
    range: ClosedRange<LocalDate>,
    courseColor: String? = null
): List<CalendarItem> {
    if (isCancelled) return emptyList()

    val seriesStart = LocalDate.parse(startDate)
    val rule = recurrenceRule

    // Evento único
    if (rule == null || rule.type == RecurrenceType.NONE) {
        if (seriesStart !in range) return emptyList()
        return listOf(buildClaseItem(seriesStart, courseColor))
    }

    val seriesEnd: LocalDate = when (rule.endType) {
        RecurrenceEndType.UNTIL_DATE ->
            rule.untilDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: LocalDate.parse(endDate)
        else -> LocalDate.parse(endDate)
    }
    val maxOccurrences = if (rule.endType == RecurrenceEndType.AFTER_OCCURRENCES)
        rule.occurrenceCount ?: Int.MAX_VALUE else Int.MAX_VALUE

    val effectiveEnd = minOf(seriesEnd, range.endInclusive)
    if (seriesStart > effectiveEnd) return emptyList()

    val result = mutableListOf<CalendarItem>()
    var occurrences = 0

    when (rule.type) {
        RecurrenceType.DAILY -> {
            var current = seriesStart
            while (current <= effectiveEnd && occurrences < maxOccurrences) {
                if (current in range) {
                    result.add(buildClaseItem(current, courseColor))
                    occurrences++
                }
                current = current.plus(rule.interval, DateTimeUnit.DAY)
            }
        }

        RecurrenceType.WEEKLY -> {
            val effectiveDays = daysOfWeek.ifEmpty { setOf(seriesStart.dayOfWeek.ordinal) }
            val baseMonday = mondayOf(seriesStart)
            var weekMonday = baseMonday
            var weekIndex = 0

            while (weekMonday <= effectiveEnd && occurrences < maxOccurrences) {
                if (weekIndex % rule.interval == 0) {
                    for (dow in effectiveDays.sorted()) {
                        if (occurrences >= maxOccurrences) break
                        val day = weekMonday.plus(dow, DateTimeUnit.DAY)
                        if (day < seriesStart || day > effectiveEnd) continue
                        if (day in range) {
                            result.add(buildClaseItem(day, courseColor))
                            occurrences++
                        }
                    }
                }
                weekMonday = weekMonday.plus(7, DateTimeUnit.DAY)
                weekIndex++
            }
        }

        RecurrenceType.MONTHLY -> {
            var current = seriesStart
            while (current <= effectiveEnd && occurrences < maxOccurrences) {
                if (current in range) {
                    result.add(buildClaseItem(current, courseColor))
                    occurrences++
                }
                current = current.plus(rule.interval, DateTimeUnit.MONTH)
            }
        }

        RecurrenceType.NONE -> { /* handled above */ }
    }

    return result
}

private fun Clase.buildClaseItem(date: LocalDate, courseColor: String?): CalendarItem =
    CalendarItem(
        id          = "${id}_${date}",
        sourceId    = id,
        type        = EventType.CLASS,
        title       = titulo.ifBlank { "Clase" },
        date        = date.toString(),
        startTime   = startTime,
        endTime     = endTime ?: startTime?.let { addOneHour(it) },
        isAllDay    = startTime == null,
        courseColor = courseColor,
        courseId    = courseId
    )

// ── Tarea ────────────────────────────────────────────────────────────────────

/**
 * Genera un único [CalendarItem] para una [Tarea].
 * Si no tiene hora → [CalendarItem.isAllDay] = true e [CalendarItem.isDeadline] = true.
 */
fun Tarea.toCalendarItem(courseColor: String? = null): CalendarItem =
    CalendarItem(
        id          = id,
        sourceId    = id,
        type        = EventType.TASK,
        title       = titulo,
        date        = dueDate,
        startTime   = dueTime,
        endTime     = null,
        isAllDay    = dueTime == null,
        isDeadline  = true,
        courseColor = courseColor,
        courseId    = courseId,
        status      = estado.name
    )

// ── Examen ───────────────────────────────────────────────────────────────────

/**
 * Genera un único [CalendarItem] para un [Examen].
 * Si [Examen.horaFin] es null → se calcula como [Examen.horaInicio] + 1 hora.
 */
fun Examen.toCalendarItem(courseColor: String? = null): CalendarItem {
    val effectiveEnd = horaFin ?: horaInicio?.let { addOneHour(it) }
    return CalendarItem(
        id          = id,
        sourceId    = id,
        type        = EventType.EXAM,
        title       = titulo,
        date        = fecha,
        startTime   = horaInicio,
        endTime     = effectiveEnd,
        isAllDay    = horaInicio == null,
        courseColor = courseColor,
        courseId    = courseId,
        status      = estado.name
    )
}

// ── EventoPersonal ───────────────────────────────────────────────────────────

/**
 * Genera un único [CalendarItem] para un [EventoPersonal].
 * Si no tiene hora → [CalendarItem.isAllDay] = true.
 */
fun EventoPersonal.toCalendarItem(): CalendarItem =
    CalendarItem(
        id        = id,
        sourceId  = id,
        type      = EventType.PERSONAL,
        title     = titulo,
        date      = fecha,
        startTime = horaInicio,
        endTime   = horaFin,
        isAllDay  = horaInicio == null
    )

// ── Helpers privados ─────────────────────────────────────────────────────────

/** Devuelve el lunes de la semana que contiene [date]. */
private fun mondayOf(date: LocalDate): LocalDate =
    date.plus(-date.dayOfWeek.ordinal, DateTimeUnit.DAY)

/** Añade 1 hora a un String "HH:mm" → "HH:mm". */
internal fun addOneHour(time: String): String {
    val parts = time.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return "${((h + 1) % 24).toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
}

